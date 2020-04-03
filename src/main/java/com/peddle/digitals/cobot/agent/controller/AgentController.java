package com.peddle.digitals.cobot.agent.controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles requests for the application file upload requests
 */
@Controller
@RequestMapping("/agent/api")
public class AgentController {
	
	
	@Value("${secclient.oauth2.client.clientId}")
	String clientId;
	
	@Value("${secclient.oauth2.client.clientSecret}")
	String clientSecret;
	
	@Value("${secclient.oauth2.client.accessTokenUri}")
	String accessTokenUri;
	
	@Value("${secclient.oauth2.client.scope}")
	String scope;

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory
			.getLogger(AgentController.class);

	static final String LIBS_PATH="libs";
	static final String ALL_JARS="*";

	/**
	 * Upload single file using Spring Controller
	 */
	@RequestMapping(value = "/executescript", method = RequestMethod.POST)
	public @ResponseBody
	String uploadFileHandler(@RequestParam("File") MultipartFile file,
			@RequestParam("Body") String body,@RequestParam("JobId") String jobid,
			@RequestParam("CallBackURL") String callBackURL, @RequestParam("FileName") String fileName) {

		String name = fileName;
		if (!file.isEmpty()) {
			try {

				byte[] bytes = file.getBytes();

				// Creating the directory to store file
				String rootPath = env.getProperty("scripts.dir");
				File dir = new File(rootPath + File.separator );
				if (!dir.exists())
					dir.mkdirs();


				// Create the file on server
				File serverFile = new File(dir.getAbsolutePath()
						+ File.separator + name);
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();


				File root = new File(rootPath); 

				JSONObject obj = new JSONObject(body);
				JSONArray jsonArray = obj.getJSONArray("data");
				List<Object> list = jsonArray.toList();
				List<String> data= new ArrayList<String> ();

				for(Object a: list){
					data.add(String.valueOf(a));
				} 

				URLClassLoader child = new URLClassLoader(
						new URL[] { root.toURI().toURL() }, 
						this.getClass().getClassLoader()
						);

				String scriptclassFile = name.replace(".class","");

//								Class classToLoad = Class.forName(scriptclassFile, true, child);
//								Method method =  classToLoad.getMethod("runTest", java.util.List.class,String.class,String.class);
//								Object instance = classToLoad.newInstance();
//								Object result = method.invoke(instance,data,callBackURL,jobid);


				//String scriptclassFile = name.replace(".class","");
				String classpath = System.getProperty("java.class.path");
				String cromeDriverpath = System.getProperty("webdriver.chrome.driver");
				String installDir = System.getProperty("installdir");
				logger.info(installDir);
				//				installDir = System.getenv("installdir");
				//				logger.info(installDir);
				//				installDir= installDir.replace("\"", "");


				String seliniumJarPath = installDir+File.separatorChar+LIBS_PATH+File.separatorChar+ALL_JARS;

				logger.info(seliniumJarPath);

				Process runscript = Runtime.getRuntime().exec("java -cp \"" + seliniumJarPath +";"+classpath+";"+
						rootPath +"\" "+scriptclassFile + " "+body+" "+
						cromeDriverpath+" "+callBackURL+" "+ jobid + " " + accessTokenUri
						+ " "+clientId+" "+clientSecret+" "+scope);
				
				
				StreamReader errorReader = new 
						StreamReader(runscript.getErrorStream(), "ERROR");            

				StreamReader outputReader = new 
						StreamReader(runscript.getInputStream(), "OUTPUT");

				errorReader.start();
				outputReader.start();

				runscript.waitFor();

				InputStream errorStream = runscript.getErrorStream();
				StringWriter scriptRunWriter = new StringWriter();
				IOUtils.copy(errorStream, scriptRunWriter, "UTF-8");
				String runErrorStr = scriptRunWriter.toString();
				errorStream.close();

				InputStream is = runscript.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				String line = null;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}

				logger.error(runErrorStr);


				//				JSONObject obj = new JSONObject(body);
				//				String testClassName = obj.getString("TestCase");
				//				
				//				
				//				URLClassLoader child = new URLClassLoader(
				//				        new URL[] {serverFile.toURI().toURL()},
				//				        this.getClass().getClassLoader()
				//				);
				//				Class classToLoad = Class.forName(testClassName, true, child);
				//				Method method =  classToLoad.getMethod("test", String.class, String.class,String.class);
				//				Object instance = classToLoad.newInstance();
				//				Object result = method.invoke(instance,body, jobid,callBackURL);

				//				JarClassLoader jcl = new JarClassLoader();
				//				
				//				jcl.add(rootPath);
				//				JclObjectFactory factory = JclObjectFactory.getInstance();
				//				// Create object of loaded class  
				//				Object obj =  factory.create(jcl, "com.cobot.testcases.JiraAddUserTest");
				//				
				//				java.lang.reflect.Method method=null;
				//				try {
				//					System.out.println(obj.getClass().getName());
				//				  method = obj.getClass().getMethod("test", String.class, String.class);
				//				} catch (SecurityException e) { e.printStackTrace(); }
				//				  catch (NoSuchMethodException e) { e.printStackTrace();  }
				//				
				//				try {
				//					  method.invoke(obj, body, jobid);
				//					} catch (Exception e) { e.printStackTrace(); }

				logger.info("Server File Location="
						+ serverFile.getAbsolutePath());

				return "Your successfully uploaded file=" + name;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				return "You failed to upload " + name + " => " + e.getMessage();
			}
		} else {
			return "You failed to upload " + name
					+ " because the file was empty.";
		}
	}


	/**
	 * Upload multiple file using Spring Controller
	 */
	@RequestMapping(value = "/launchcmd", method = RequestMethod.POST)
	public @ResponseBody
	void launchcmd() {

		try {
			Runtime process = Runtime.getRuntime();
			process.exec("cmd /c start cmd.exe /K powershell");

			System.out.println("launched CMD");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		AgentController controller = new AgentController();

		controller.launchcmd();

	}
}
