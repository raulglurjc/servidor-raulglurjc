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
	static String cosa = null;
	private static httprequest requestToClient = new httprequest();
	
	public static void main(String[] args) throws
	ClassNotFoundException, SQLException, URISyntaxException {
		port(getHerokuAssignedPort());
		File uploadDir = new File("upload/");
		uploadDir.mkdir(); // create the upload directory if it doesn't exist
		staticFiles.externalLocation("upload");
		
		
		//	int examen=0;//Si es 0 el examen esta finalizado, 1 está activo.

	Random rnd = new Random();

	redirect.get("/", "/profesor");
	get("/upload", (req, res) -> 
    "<form action='/examen' method='post' enctype='multipart/form-data'>"
	+ "Nombre: <input type='text' name='nombre' required='true'><br><br>"
    + "DNI: <input type='text' name='dni' required='true'><br><br>"
	+ "id examen: <input type='text' name='idex' required='true'><br><br>"
    
    + "<input type='file' name='uploaded_films_file' accept='.zip'>"
    + "    <button>Upload file</button>" + "</form>");
	
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
	
	get("/profesor", (req, res) -> {

		String result = "<form action='/profesor' method='post'>"
		+ "<fieldset>"
		+ "<p>INTRODUZCA LOS DATOS:</p>\n"

		+ "<p>Asignatura:  "
		+ "<input type='text' name='asignatura' required='true'><br><br>"
		+ "<input type=\"submit\" value=\"Comenzar examen\">"
			+ "</fieldset>"
			+ "</form></p>";

		return result;
	});

	post("/alumno", (req, res) -> {
		alumnoDao alumnoDao = new alumnoDao();
		realizaExamenDao realizaExamenDao = new realizaExamenDao();
		String dni = req.queryParams("dni");
		String nombre = req.queryParams("nombre");
		int id_ex = Integer.parseInt(req.queryParams("idex"));
		
	    String ip = req.ip(); //IP de la petición
		int port = req.port(); //PUERTO de la petición
		
		alumno alumnoObject = new alumno(dni,nombre, port, ip);
		alumnoDao.save(alumnoObject);
		realizaExamen realizaExamenObject = new realizaExamen(id_ex,dni, null);
		realizaExamenDao.save(realizaExamenObject);
		alumnoDao.close();
		realizaExamenDao.close();
		System.out.println("*******************************************************************");
		System.out.println("POST recibido del alumno: "+nombre+" para el examen: "+id_ex+" con IP: "+ip+":"+port);
		System.out.println("*******************************************************************");
		
		return "EXITO";
	});
	


	get("/cosa", (req, res) ->
		"<h1> El examen con ID "+ cosa + "</h1>"
	);


	post("/profesor", (req, res) -> { // Revisar si es get o post
		examenDao examenDao = new examenDao();
		int id_examen = (int) (Math.random()*1000000000 +1);


		//Añadido

		String asignatura = req.queryParams("asignatura");
		//-Añadido
		String result ="<h1> Examen de la asignatura <strong style='color:red'>"+ asignatura + "</strong> creado con <u>éxito</u></h1>"
		+ "<form action='/"+id_examen+"' method='get'>"
			+ "<input type=\"submit\" value=\"Finalizar examen\">"
			+ "</form><br>"
			+"<h2>Se ha generado el examen en la url "+id_examen+"</h2>";


		//Añadido
		Date fecha = new Date();
		long lnMilisegundos = fecha.getTime();
		java.sql.Date sqlDate = new java.sql.Date(lnMilisegundos);

		examen examenObject = new examen(id_examen, sqlDate, asignatura);
		examenDao.save(examenObject);

		
		List<examen> allExamenes = new ArrayList<examen>();
		allExamenes = examenDao.all();
		System.out.println("*******************************************************************");
		System.out.println("POST recibido para iniciar examen de la asignatura "+asignatura+" con ID: "+id_examen);
		System.out.println("*******************************************************************");
		
		result = result + "Lista de Examenes de la BD:<br>";
		for (int i=0;i<allExamenes.size();i++) {
		      
			result = result + " &nbsp; &nbsp- "+allExamenes.get(i).getIdExamen()+" "+allExamenes.get(i).getAsignatura()+"<br>";
		    }
		//-Añadido

		
		return result;
	});
	get("/prueba", (req, res) -> {		
		requestToClient.sendGetprueba();

		return "EXITOS";
	});
	get("/:random", (req, res) -> {
		examenDao examenDao = new examenDao();
		//COMPROBAR SI EL RECURSO :RANDOM SE ENCUENTRA EN LA BD, SI NO ES ASI, DEVOLVER 404 NOT FOUND
		if(!examenDao.comprobar_examen(req.params(":random")))			
			halt(404, "404 NOT FOUND");
		//BUCLE QUE RECORRA LOS ALUMNOS DEL ID DE EXAMEN HACIENDO GET A CADA UNO
		//requestToClient.sendGetAlumno();
		
		String result = "<h1>Examen con id "+req.params(":random")+" finalizado!</h1>"
				+"<h2>Espera unos minutos hasta que se genere el informe de copias.</h2>";

		return result;
	});
	

		

//		get("/alumnos", (req, res) ->
//			String result = "<form action='/examinar' method='post'>"
//			+ "<fieldset>"
//			+ "<p>INTRODUZCA LOS DATOS:</p>\n"
//			+ "<p>Nombre: <input type='text' name='nombre_alumno' required='true'></p>\n"
//			+ "<p>Apellidos: <input type='text' name='apellido_alumno' required='true'></p>\n"
//			+ "<p>ID de examen: <input type='text' name='id_examen_alumno' required='true'></p>\n"
//			+ "<input type=\"submit\" value=\"Comenzar examen\"></fieldset></form>";
//
//			return result;
//		);
//
//		
//
		
		
		
		
		
		
		
		
		
	}

	static int getHerokuAssignedPort() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (processBuilder.environment().get("PORT") != null) {
		    return Integer.parseInt(processBuilder.environment().get("PORT"));
		}
		return 4567; // return default port if heroku-port isn't set (i.e. on localhost)
	    }
	private static String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
