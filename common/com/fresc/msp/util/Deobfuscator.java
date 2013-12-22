package com.fresc.msp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.minecraftforge.common.ForgeVersion;

public class Deobfuscator
{

  public static final String MINECRAFT_VERSION = "1.6.4";
  
  public static final String FORGE_VERSION = Downloader.getForgeVersion();
  
  public static final String ARCHIVE_URL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/"+FORGE_VERSION+"/forge-"+FORGE_VERSION+"-src.zip";
  
  public static final String PATH_IN_ARCHIVE = "forge/fml/conf/";
  
  public static final String OUTPUT_PATH = "./";
  
  public static final String[] FILES_IN_ARCHIVE = {
    "joined.srg",
    "fields.csv",
    "methods.csv"
  };
  
  public static final String VERSION_TOKEN = "mincraftforge-debfuscationdata.version";

  private static final Pattern PACKAGE_EXPR = Pattern.compile("PK:\\s([^\\s]*)\\s([^\\s]*)");
  
  private static final Pattern CLASS_EXPR = Pattern.compile("CL:\\s([^\\s]*)\\s([^\\s]*)");
  
  private static final Logger LOGGER = Logger.getLogger(Deobfuscator.class.getName());
  
  private static Deobfuscator instance = null;
  
  public static class Downloader
  {
    
    private Downloader()
    {
    }
    
    private static String getForgeVersion()
    {
      return String.format("%s-%s", MINECRAFT_VERSION, ForgeVersion.getVersion());
    }
    
    public static void download() throws IOException
    {
      if (!doVersionsMatch() && !alreadyDownloaded())
      {
        cleanupOldVersionFiles();
        URL url = new URL(ARCHIVE_URL);
        InputStream input = url.openStream();
        ZipInputStream zipStream = new ZipInputStream(input);
        ZipEntry entry;
        int numExtracted = 0;
        while ((numExtracted < FILES_IN_ARCHIVE.length) && ((entry = zipStream.getNextEntry()) != null))
        {
          if (!entry.isDirectory())
          {
            for (String filename : FILES_IN_ARCHIVE)
            {
              if (PATH_IN_ARCHIVE.concat(filename).contentEquals(entry.getName()))
              {
                LOGGER.log(Level.INFO, "Downloader: extracting "+entry.getName());
                FileOutputStream output = null;
                try
                {
                  output = new FileOutputStream(OUTPUT_PATH + filename);
                  streamCopy(zipStream, output);
                  output.flush();
                } finally {
                  if (output != null)
                    output.close();
                }
                ++numExtracted;
              }
            }
          }
          zipStream.closeEntry();
        }
        zipStream.close();
        storeVersionToken();
      } else
      {
        LOGGER.log(Level.INFO, "Downloader: already downloaded");
      }
    }

    private static void cleanupOldVersionFiles()
    {
      for (String filename : FILES_IN_ARCHIVE)
        try
        {
          Files.deleteIfExists(Paths.get(OUTPUT_PATH, filename));
        } catch (IOException e)
        {
          LOGGER.throwing(Downloader.class.getName(), "cleanupOldVersionFiles", e);
        }
      try
      {
        Files.deleteIfExists(Paths.get(OUTPUT_PATH, VERSION_TOKEN));
      } catch (IOException e)
      {
        LOGGER.throwing(Downloader.class.getName(), "cleanupOldVersionFiles", e);
      }
    }

    private static void storeVersionToken() throws IOException
    {
      ByteArrayInputStream input = new ByteArrayInputStream(FORGE_VERSION.getBytes());
      FileOutputStream output = null;
      try
      {
        output = new FileOutputStream(OUTPUT_PATH + VERSION_TOKEN);
        streamCopy(input, output);
        output.flush();
      } catch (IOException e)
      {
        LOGGER.throwing(Downloader.class.getName(), "storeVersionToken", e);
      } finally
      {
        if (output != null)
          output.close();
      }
    }

    private static boolean doVersionsMatch() throws IOException
    {
      FileInputStream input = null;
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      boolean result = false;
      try
      {
        input = new FileInputStream(OUTPUT_PATH + VERSION_TOKEN);
        streamCopy(input, output);
        output.flush();
        result = Arrays.equals(output.toByteArray(), FORGE_VERSION.getBytes());
      } catch (IOException e)
      {
        LOGGER.throwing(Downloader.class.getName(), "doVersionsMatch", e);
      } finally
      {
        if (input != null)
          input.close();
      }
      return result;
    }

    private static boolean alreadyDownloaded()
    {
      int numFound = 0;
      for (String filename : FILES_IN_ARCHIVE)
        if (Files.exists(Paths.get(OUTPUT_PATH, filename)))
          ++numFound;
      return numFound == FILES_IN_ARCHIVE.length;
    }

    private static void streamCopy(InputStream input, OutputStream output) throws IOException
    {
      fastChannelCopy(Channels.newChannel(input), Channels.newChannel(output));
    }

    private static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
      final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
      while (src.read(buffer) != -1) {
        // prepare the buffer to be drained
        buffer.flip();
        // write to the channel, may block
        dest.write(buffer);
        // If partial transfer, shift remainder down
        // If buffer is empty, same as doing clear()
        buffer.compact();
      }
      // EOF will leave buffer in fill state
      buffer.flip();
      // make sure the buffer is fully drained.
      while (buffer.hasRemaining()) {
        dest.write(buffer);
      }
    }

  }
  
	public abstract class BaseInfo
	{
		
		public final String obfuscatedName;
		public final String deobfuscatedName;
		
		private BaseInfo(String obfuscatedName, String deobfuscatedName)
		{
			this.obfuscatedName = obfuscatedName;
			this.deobfuscatedName = deobfuscatedName;
		}
		
		public String getNormalizedObfuscatedName()
		{
			return obfuscatedName.replace('/', '.');
		}
		
		public String getNormalizedDeobfuscatedName()
		{
			return deobfuscatedName.replace('/', '.');
		}
		
		public String getSimpleObfuscatedName()
		{
			String normalizedObfuscatedName = getNormalizedObfuscatedName();
			final String[] path = normalizedObfuscatedName.split("\\.");
			if (path.length == 0)
				return normalizedObfuscatedName;
			return path[path.length - 1];
		}

		public String getSimpleDeobfuscatedName()
		{
			String normalizedDeobfuscatedName = getNormalizedDeobfuscatedName();
			final String[] path = normalizedDeobfuscatedName.split("\\.");
			if (path.length == 0)
				return normalizedDeobfuscatedName;
			return path[path.length - 1];
		}

		@Override
		public String toString()
		{
			return "[Info '" + obfuscatedName + "' -> '" + deobfuscatedName + "']";
		}
		
	}
	
	public final class PackageInfo extends BaseInfo
	{
		
		private PackageInfo(String obfuscatedName, String deobfuscatedName)
		{
			super(obfuscatedName, deobfuscatedName);
		}
		
	}
	
	public final class ClassInfo extends BaseInfo
	{
		
		private ClassInfo(String obfuscatedName, String deobfuscatedName)
		{
			super(obfuscatedName, deobfuscatedName);
		}
		
	}
	
	public final class FieldInfo extends BaseInfo
	{
		
		private FieldInfo(String obfuscatedName, String deobfuscatedName)
		{
			super(obfuscatedName, deobfuscatedName);
		}
		
	}
	
	public final class MethodInfo extends BaseInfo
	{
		
		public final String obfuscatedSignature;
		public final String deobfuscatedSignature;
		
		private MethodInfo(String obfuscatedName, String obfuscatedSignature, String deobfuscatedName, String deobfuscatedSignature)
		{
			super(obfuscatedName, deobfuscatedName);
			this.obfuscatedSignature = obfuscatedSignature;
			this.deobfuscatedSignature = deobfuscatedSignature;
		}
		
	}
	
	public class InfoTable< I extends BaseInfo > extends Vector< I >
	{
		
		/**
     * 
     */
    private static final long serialVersionUID = 1L;

    private InfoTable()
		{
		}
		
		public I lookupObfuscatedName(String obfuscatedName)
		{
			obfuscatedName = obfuscatedName.replace('.', '/');
			for (I info : this)
			{
				if (info.obfuscatedName.equals(obfuscatedName))
					return info;
			}
			return null;
		}
		
		public I lookupDeobfuscatedName(String deobfuscatedName)
		{
			deobfuscatedName = deobfuscatedName.replace('.', '/');
			for (I info : this)
			{
				if (info.deobfuscatedName.equals(deobfuscatedName))
					return info;
			}
			return null;
		}
		
	}
	
	public class MethodInfoTable extends InfoTable< MethodInfo >
	{
		
		/**
     * 
     */
    private static final long serialVersionUID = 1L;

    private MethodInfoTable()
		{
		}
		
		public MethodInfo lookupObfuscatedName(String obfuscatedName, String obfuscatedSignature)
		{
			obfuscatedName = obfuscatedName.replace('.', '/');
			for (MethodInfo info : this)
			{
				if (info.obfuscatedName.equals(obfuscatedName) && info.obfuscatedSignature.equals(obfuscatedSignature))
					return info;
			}
			return null;
		}
		
		public MethodInfo lookupDeobfuscatedName(String deobfuscatedName, String deobfuscatedSignature)
		{
			deobfuscatedName = deobfuscatedName.replace('.', '/');
			for (MethodInfo info : this)
			{
				if (info.deobfuscatedName.equals(deobfuscatedName) && info.deobfuscatedSignature.equals(deobfuscatedSignature))
					return info;
			}
			return null;
		}
		
	}
	
	private static InputStream getDatabaseStream(String filename)
	{
	  try
    {
      return new FileInputStream(filename);
    } catch (FileNotFoundException e)
    {
    }
	  return null;
	}
	
	private static Reader createDatabaseReader()
	{
		final InputStream resourceStream = getDatabaseStream(OUTPUT_PATH + FILES_IN_ARCHIVE[0]);
		if (resourceStream == null)
			return null;
		return new InputStreamReader(resourceStream);
	}
	
	private static Reader createFieldDatabaseReader()
	{
		final InputStream resourceStream = getDatabaseStream(OUTPUT_PATH + FILES_IN_ARCHIVE[1]);
		if (resourceStream == null)
			return null;
		return new InputStreamReader(resourceStream);
	}
	
	private static Reader createMethodDatabaseReader()
	{
		final InputStream resourceStream = getDatabaseStream(OUTPUT_PATH + FILES_IN_ARCHIVE[2]);
		if (resourceStream == null)
			return null;
		return new InputStreamReader(resourceStream);
	}
	
	private static String simplify(String name)
	{
		String[] parts = name.trim().replace('.', '/').split("/");
		if ((parts == null) || (parts.length == 0))
			return name;
		return parts[parts.length-1];
	}
	
	public static Deobfuscator getInstance()
	{
		if (instance == null)
		{
			instance = new Deobfuscator();
			try
			{
				instance.load();
			} catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "error while loading deobfuscation data", e);
			}
		}
		return instance;
	}
	
	private final InfoTable< PackageInfo > packages = new InfoTable< PackageInfo >();
	private final InfoTable< ClassInfo > classes = new InfoTable< ClassInfo >();
	private final InfoTable< FieldInfo > fields = new InfoTable< FieldInfo >();
	private final MethodInfoTable methods = new MethodInfoTable();
	
	private Deobfuscator()
	{
    try
    {
      Downloader.download();
    } catch (IOException e)
    {
    }
	}
	
	public void load() throws IOException, ParseException
	{
		packages.clear();
		classes.clear();
		fields.clear();
		methods.clear();
		
		int loaded = 0;
		
		final LineNumberReader fieldsReader = new LineNumberReader(createFieldDatabaseReader());
		if (fieldsReader != null)
		{
			try
			{
				String[] cols = null;
				String[] data;
				String line;
				while (null != (line = fieldsReader.readLine()))
				{
					line = line.trim();
					data = line.split(",");
					if (cols == null) {
						cols = data;
						continue;
					}
					fields.add(new FieldInfo(data[0], data[1]));
				}
			} finally {
				fieldsReader.close();
				++loaded;
			}
		}
		
		final LineNumberReader methodsReader = new LineNumberReader(createMethodDatabaseReader());
		if (methodsReader != null)
		{
			try
			{
				String[] cols = null;
				String[] data;
				String line;
				while (null != (line = methodsReader.readLine()))
				{
					line = line.trim();
					data = line.split(",");
					if (cols == null) {
						cols = data;
						continue;
					}
					methods.add(new MethodInfo(data[0], null, data[1], null));
				}
			} finally {
				methodsReader.close();
				++loaded;
			}
		}
		
		final LineNumberReader reader = new LineNumberReader(createDatabaseReader());
		if (reader != null)
		{
			try
			{
				Matcher m;
				String line;
				while (null != (line = reader.readLine()))
				{
					line = line.trim();
					if (line.isEmpty())
						continue;
					
					m = PACKAGE_EXPR.matcher(line);
					if (m.matches())
					{
						packages.add(new PackageInfo(m.group(1), m.group(2)));
						continue;
					}
					
					m = CLASS_EXPR.matcher(line);
					if (m.matches())
					{
						classes.add(new ClassInfo(m.group(1), m.group(2)));
						continue;
					}
					
					/*
					m = FIELD_EXPR.matcher(line);
					if (m.matches())
					{
						String simpleFieldname = simplify(m.group(2));
						fields.add(new FieldInfo(m.group(1), m.group(2), fieldTranslator.getProperty(simpleFieldname, simpleFieldname)));
						continue;
					}
					
					m = METHOD_EXPR.matcher(line);
					if (m.matches())
					{
						String simpleMethodname = simplify(m.group(3));
						methods.add(new MethodInfo(m.group(1), m.group(2), m.group(3), methodTranslator.getProperty(simpleMethodname, simpleMethodname), m.group(4)));
						continue;
					}
					*/
					
					// throw new ParseException("unable to parse line " + reader.getLineNumber(), reader.getLineNumber());
				}
			} finally
			{
				reader.close();
				++loaded;
			}
			
		}
		
		if (loaded == 3)
		{
      LOGGER.log(Level.INFO, "successfully loaded");
      LOGGER.log(Level.INFO, "\t packages: " + packages.size());
      LOGGER.log(Level.INFO, "\t classes:  " + classes.size());
      LOGGER.log(Level.INFO, "\t fields:   " + fields.size());
      LOGGER.log(Level.INFO, "\t methods:  " + methods.size());
		} else
		{
		  LOGGER.log(Level.INFO, "unable to load deobfuscation data");
		}
		
	}
	
	public String deobfuscatePackageName(String packageName)
	{
	  LOGGER.log(Level.FINEST, "deobfuscatePackageName "+packageName);
		final PackageInfo info = packages.lookupObfuscatedName(packageName);
		if (info == null)
			return packageName;
		return info.getNormalizedDeobfuscatedName();
	}
	
	public String deobfuscateClassName(String className)
	{
    LOGGER.log(Level.FINEST, "deobfuscateClassName "+className);
		final ClassInfo info = classes.lookupObfuscatedName(className);
		if (info == null)
			return className;
		return info.getNormalizedDeobfuscatedName();
	}
	
	public String deobfuscateFieldName(String fieldName)
	{
    LOGGER.log(Level.FINEST, "deobfuscateFieldName "+fieldName);
		final FieldInfo info = fields.lookupObfuscatedName(fieldName);
		if (info == null)
			return fieldName;
		return info.getNormalizedDeobfuscatedName();
	}
	
	public String deobfuscateFieldNameSimple(String fieldName)
	{
    LOGGER.log(Level.FINEST, "deobfuscateFieldNameSimple "+fieldName);
		final FieldInfo info = fields.lookupObfuscatedName(fieldName);
		if (info == null)
			return simplify(fieldName);
		return info.getSimpleDeobfuscatedName();
	}
	
	public String deobfuscateMethodName(String methodName)
	{
    LOGGER.log(Level.FINEST, "deobfuscateMethodName "+methodName);
		final MethodInfo info = methods.lookupObfuscatedName(methodName);
		if (info == null)
			return methodName;
		return info.getNormalizedDeobfuscatedName();
	}
	
	public String deobfuscateMethodName(String methodName, String signature)
	{
    LOGGER.log(Level.FINEST, "deobfuscateMethodName "+methodName+" "+signature);
		final MethodInfo info = methods.lookupObfuscatedName(methodName, signature);
		if (info == null)
			return methodName;
		return info.getNormalizedDeobfuscatedName();
	}
	
	public String deobfuscateMethodNameSimple(String methodName)
	{
    LOGGER.log(Level.FINEST, "deobfuscateMethodNameSimple "+methodName);
		final MethodInfo info = methods.lookupObfuscatedName(methodName);
		if (info == null)
			return simplify(methodName);
		return info.getSimpleDeobfuscatedName();
	}
	
	public String deobfuscateMethodNameSimple(String methodName, String signature)
	{
    LOGGER.log(Level.FINEST, "deobfuscateMethodNameSimple "+methodName+" "+signature);
		final MethodInfo info = methods.lookupObfuscatedName(methodName, signature);
		if (info == null)
			return simplify(methodName);
		return info.getSimpleDeobfuscatedName();
	}
	
	public String obfuscatePackageName(String packageName)
	{
    LOGGER.log(Level.FINEST, "obfuscatePackageName "+packageName);
		final PackageInfo info = packages.lookupDeobfuscatedName(packageName);
		if (info == null)
			return packageName;
		return info.getNormalizedObfuscatedName();
	}
	
	public String obfuscateClassName(String className)
	{
    LOGGER.log(Level.FINEST, "obfuscateClassName "+className);
		final ClassInfo info = classes.lookupDeobfuscatedName(className);
		if (info == null)
			return className;
		return info.getNormalizedObfuscatedName();
	}
	
	public String obfuscateFieldName(String fieldName)
	{
    LOGGER.log(Level.FINEST, "obfuscateFieldName "+fieldName);
		final FieldInfo info = fields.lookupDeobfuscatedName(fieldName);
		if (info == null)
			return fieldName;
		return info.getNormalizedObfuscatedName();
	}
	
	public String obfuscateFieldNameSimple(String fieldName)
	{
    LOGGER.log(Level.FINEST, "obfuscateFieldNameSimple "+fieldName);
		final FieldInfo info = fields.lookupDeobfuscatedName(fieldName);
		if (info == null)
			return simplify(fieldName);
		return info.getSimpleObfuscatedName();
	}
	
	public String obfuscateMethodName(String methodName)
	{
    LOGGER.log(Level.FINEST, "obfuscateMethodName "+methodName);
		final MethodInfo info = methods.lookupDeobfuscatedName(methodName);
		if (info == null)
			return methodName;
		return info.getNormalizedObfuscatedName();
	}
	
	public String obfuscateMethodName(String methodName, String signature)
	{
    LOGGER.log(Level.FINEST, "obfuscateMethodName "+methodName+" "+signature);
		final MethodInfo info = methods.lookupDeobfuscatedName(methodName, signature);
		if (info == null)
			return methodName;
		return info.getNormalizedObfuscatedName();
	}
	
	public String obfuscateMethodNameSimple(String methodName)
	{
    LOGGER.log(Level.FINEST, "obfuscateMethodNameSimple "+methodName);
		final MethodInfo info = methods.lookupDeobfuscatedName(methodName);
		if (info == null)
			return simplify(methodName);
		return info.getSimpleObfuscatedName();
	}
	
	public String obfuscateMethodNameSimple(String methodName, String signature)
	{
    LOGGER.log(Level.FINEST, "obfuscateMethodNameSimple "+methodName+" "+signature);
		final MethodInfo info = methods.lookupDeobfuscatedName(methodName, signature);
		if (info == null)
			return simplify(methodName);
		return info.getSimpleObfuscatedName();
	}
	
}
