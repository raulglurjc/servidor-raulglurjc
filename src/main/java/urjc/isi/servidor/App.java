package urjc.isi.servidor;

import static spark.Spark.*;
//import spark.Request;
//import spark.Response;
import urjc.isi.servidor.App;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
//import java.sql.Statement;
//import java.sql.PreparedStatement;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.MultipartConfigElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import static spark.Spark.*;
import spark.*;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import spark.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import static spark.Spark.*;
public class App
{
	private static final Map<String, Object> settings = new HashMap<String, Object>();

	  public static String parse(String pattern, String text, Map<String, Object> locals) {
	    Matcher regexp = Pattern.compile(pattern).matcher(text);
	    while (regexp.find()) {
	      text = regexp.replaceFirst(locals.get(regexp.group(1)).toString());
	    }
	    return text;
	  }

	  public static String parseFile(String file, String pattern, Map<String, Object> locals) {
	    StringBuffer content = new StringBuffer("");
	    try {
	      BufferedReader buffer = new BufferedReader(new FileReader(file));
	      String line = null;

	      while ((line = buffer.readLine()) != null) {
	        content.append(parse(pattern, line, locals) + "\n");
	      }

	      buffer.close();
	    }
	    catch (Exception exception) {
	      System.out.printf("ERROR: %s\n", exception.getMessage());
	    }
	    finally {
	      return content.toString();
	    }
	  }

	  public static String render(String file, Map<String, Object> locals) {
	    return layout(file, parseFile(file, "\\$\\{(\\w.*?)\\}", locals));
	  }

	  public static String layout(String file, String content) {
	    HashMap<String, Object> layout = new HashMap<String, Object>();
	    layout.put("content", content);
	    return parseFile("views/layout.html", "@\\{(content)\\}", layout);
	  }

	  public static void set(String key, Object value) {
	    settings.put(key, (String) value);
	  }

	  public static Object settings(String key) {
	    return settings.get(key);
	  }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static httprequest requestToClient = new httprequest();
	
	public static void main(String[] args) throws
	ClassNotFoundException, SQLException, URISyntaxException {
		
		port(getHerokuAssignedPort());
		File uploadDir = new File("upload/");
		uploadDir.mkdir(); // create the upload directory if it doesn't exist
		staticFiles.externalLocation("upload");

		examenDao examenDao = new examenDao();
		realizaExamenDao realizaExamenDao = new realizaExamenDao();
		alumnoDao alumnoDao = new alumnoDao();
		
		redirect.get("/", "/profesor");
		
		get("/profesor", (req, res) -> {		
			return render("views/index.html", settings);
		});
		
		
		post("/profesor", (req, res) -> { 
			
			int id_examen = (int) (Math.random()*1000000000 +1); // Numero aleatorio que se asignará al examen
			
			String asignatura = req.queryParams("asignatura"); // Sacamos la query string del campo asignatura
			Date fecha = new Date();
			long lnMilisegundos = fecha.getTime();
			java.sql.Date sqlDate = new java.sql.Date(lnMilisegundos);
			
			examen examenObject = new examen(id_examen, sqlDate, asignatura);
			examenDao.save(examenObject); //Inserción del examen en la BD		
			
			System.out.println("*******************************************************************");
			System.out.println("POST recibido para iniciar examen de la asignatura "+asignatura+" con ID: "+id_examen);
			System.out.println("*******************************************************************");
			
			List<examen> allExamenes = new ArrayList<examen>();
			allExamenes = examenDao.all();
			
			
			
			
			String result="";
			for (int i=0;i<allExamenes.size();i++) {
			      
				result = result + "&nbsp &nbsp- "+allExamenes.get(i).getIdExamen()+" "+allExamenes.get(i).getAsignatura()+"<br>";
			    }

			set("id_examen", String.valueOf(id_examen));
			set("bloque_examenes", result);
			return render("views/post_profesor.html", settings);
		});
		
		
		
		
			
	post("/examen", (req, res) -> {		
		
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        String dni = req.raw().getParameter("dni");
        String nombre = req.raw().getParameter("nombre");
        int id_ex = Integer.parseInt(req.raw().getParameter("idex"));
        System.out.println("*******************************************************************");
        System.out.println("POST recibido del alumno: "+nombre+" para finalizar el examen: "+id_ex);
        System.out.println("*******************************************************************");
        File aux = new File("upload/"+id_ex);
        aux.mkdir();
        
        Path tempFile = Files.createTempFile(aux.toPath(), nombre+"_"+dni+"_"+id_ex+"_", "");
        try (InputStream input = req.raw().getPart("file").getInputStream()) { // getPart needs to use same "name" as input field in form
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return "EXITO";
	});
	
	

	post("/alumno", (req, res) -> {
		
		String dni = req.queryParams("dni");
		String nombre = req.queryParams("nombre");
		int id_ex = Integer.parseInt(req.queryParams("idex"));
		
	    String ip = req.ip(); //IP de la petición
		int port = req.port(); //PUERTO de la petición
		
		alumno alumnoObject = new alumno(dni,nombre, port, ip);
		alumnoDao.save(alumnoObject);
		realizaExamen realizaExamenObject = new realizaExamen(id_ex,dni, null);
		realizaExamenDao.save(realizaExamenObject);
		
		System.out.println("*******************************************************************");
		System.out.println("POST recibido del alumno: "+nombre+" para el examen: "+id_ex+" con IP: "+ip+":"+port);
		System.out.println("*******************************************************************");
		
		return "EXITO";
	});


	get("/:random", (req, res) -> {
		
		//COMPROBAR SI EL RECURSO :RANDOM SE ENCUENTRA EN LA BD, SI NO ES ASI, DEVOLVER 404 NOT FOUND
		
		if(examenDao.comprobar_examen(Integer.parseInt(req.params(":random")))==0)			
			halt(404, "404 NOT FOUND");
		
		//BUCLE QUE RECORRA LOS ALUMNOS DEL ID DE EXAMEN HACIENDO GET A CADA UNO
		requestToClient.sendGetAlumno();
		
		String result = "<h1>Examen con id "+req.params(":random")+" finalizado!</h1>"
				+"<h2>Espera unos minutos hasta que se genere el informe de copias.</h2>";

		return result;
	});
	
		
	}

	static int getHerokuAssignedPort() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (processBuilder.environment().get("PORT") != null) {
		    return Integer.parseInt(processBuilder.environment().get("PORT"));
		}
		return 4567; // return default port if heroku-port isn't set (i.e. on localhost)
	    }	
}
