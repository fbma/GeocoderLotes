import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import commandline.CmdLn;
import commandline.CmdLnOption;

import es.juntadeandalucia.callejero.ws.CallejeroService;
import es.juntadeandalucia.callejero.ws.CallejeroServiceProxy;
import es.juntadeandalucia.callejero.dto.Address;
import es.juntadeandalucia.callejero.dto.GeocoderResult;



public class BatchGeocoder {
	
	static String filePath = "fichero.csv";
	static String fileResult = "fichero_resultados.csv";
	static String fileNotFound = "fichero_not_geocoded.csv";
	static String fileCasosProducidos = "fichero_log.txt";
	
	static String separador = "\\%";
	
	static boolean tieneCabecera = false;
	
	// Los siguientes parametros lo lee del fichero de propiedades, estos valores solo se usan en caso
	// de que no esten correctamente inicializados
	static String urlGeocoder = "http://www.callejerodeandalucia.es/ws/services/InterfazCDAUWS?wsdl";
	static String projection = "EPSG:25830";
	static String units = "m";
	static int[] fields;
	
	static boolean codigoIne = false;
	
	static int campoCodigoIne;
	
	static Properties prop = new Properties();
	static{
		
		InputStream stream = 
			BatchGeocoder.class.getResourceAsStream("batchgeocoder.properties");
		try {
			prop.load(stream);
			urlGeocoder = (String) prop.get("url_geocoder");
			projection = (String)  prop.get("projection");
			units = (String)  prop.get("units");
		} catch (IOException e) {
			System.out.println("Error al cargar el fichero de propiedades");
			e.printStackTrace();
		}		
		
	}
	
	private static void processCommandLineArguments(String[] args){
		 CmdLn cmdLn = new CmdLn(args).setDescription("Geocodificador por lotes de l�nea" +
		 			" de comandos del proyecto SIG Corporativo " +
		 			"de la Junta de Andaluc�a");
	        cmdLn.addOptions(new CmdLnOption[]{
	        		new CmdLnOption("ficheroEntrada",'f').setRequiredArgument().setDescription("fichero con registros a geocodificar"),
	        		new CmdLnOption("ficheroResultados",'r').setRequiredArgument().setDescription("fichero de resultados"),
	        		new CmdLnOption("ficheroNoEncontrados",'n').setRequiredArgument().setDescription("fichero de direcciones no encontradas"),
	        		new CmdLnOption("separador",'s').setOptionalArgument().setDescription("separador de campos del fichero de entrada"),
	        		new CmdLnOption("tieneCabecera",'h').setOptionalArgument().setDescription("indica si hay que saltar la primera linea del fichero CSV"),
	        		new CmdLnOption("camposDireccion",'d').setRequiredArgument().setDescription("Campos que constituyen la direcci�n separados por comas, empezando por cero. Si el municipio viene en forma provincia-municipio estos dos campos deben ser especificados tambi�n"),
	        		new CmdLnOption("codigoIne",'i').setRequiredArgument().setDescription("Especificar si el municipio viene especificado por codigo INE (valor 1) o por par provincia-municipio (valor 0)"),
	        		new CmdLnOption("campoCodigoIne",'m').setOptionalArgument().setDescription("Campo que contiene el c�digo INE del municipio (empezando por cero)"),
	        		new CmdLnOption("ficheroLog",'l').setOptionalArgument().setDescription("fichero de log")
	        });
	        
	        if(cmdLn.getResult("camposDireccion") != null){
	            if (cmdLn.getResult("camposDireccion").getArgumentCount() > 0){
	                String campoStr = cmdLn.getResult("camposDireccion").getArgument();
	                String[] campos = campoStr.split(",");
	                if(campos.length == 0)
	                {
	                	System.out.println("Parametro camposDireccion sin valores relevantes");
	                	System.exit(-2);
	                }
	                fields = new int[campos.length];
	                for(int i = 0; i < campos.length; i++){
	                	fields[i] = Integer.parseInt(campos[i]);
	                }
	            }else{
	            	System.out.println("No se ha especificado el valor del par�metro campos Direcciones");
	            	System.exit(-2);
	            }
	        }else{
	        	System.out.println("Es necesario especificar el argumento 'camposDirecciones' o 'd'");
            	System.exit(-2);
	        }
	        
	        if(cmdLn.getResult("codigoIne") != null){
	            if (cmdLn.getResult("codigoIne").getArgumentCount() > 0){
	                String campoStr = cmdLn.getResult("codigoIne").getArgument();
	                codigoIne = Integer.parseInt(campoStr) == 1;
	                
	            }else{
	            	System.out.println("No se ha especificado el valor del par�metro codigoIne (1 � 0)");
	            	System.exit(-2);
	            }
	        }else{
	        	System.out.println("Es necesario especificar el argumento 'codigoIne' o 'i'");
            	System.exit(-2);
	        }
	        
	        if(codigoIne){
	        	if(cmdLn.getResult("campoCodigoIne") != null){
		            if (cmdLn.getResult("campoCodigoIne").getArgumentCount() > 0){
		                String campoStr = cmdLn.getResult("campoCodigoIne").getArgument();
		                campoCodigoIne = Integer.parseInt(campoStr);
		            }else{
		            	System.out.println("No se ha especificado el valor del par�metro campoCodigoIne, siendo necesario pues el parametro codigoIne toma el valor 1");
		            	System.exit(-2);
		            }
		        }else{
		        	System.out.println("Es necesario especificar el argumento 'campoCodigoIne' o 'm'");
	            	System.exit(-2);
		        }
	        }
	        
	        
	       
	        if(cmdLn.getResult("ficheroEntrada") != null){
	            if (cmdLn.getResult("ficheroEntrada").getArgumentCount() > 0){
	                filePath = cmdLn.getResult("ficheroEntrada").getArgument();
	            }else{
	            	System.out.println("No se ha especificado el fichero de entrada");
	            	System.exit(-2);
	            }
	        }else{
	        	System.out.println("Es necesario especificar el argumento 'ficheroEntrada' o 'f'");
            	System.exit(-2);
	        }
	        
	        if(cmdLn.getResult("ficheroResultados") != null){
	            if (cmdLn.getResult("ficheroResultados").getArgumentCount() > 0){
	                fileResult = cmdLn.getResult("ficheroResultados").getArgument();
	            }else{
	            	System.out.println("No se ha especificado el fichero de resultados");
	            	System.exit(-2);
	            }
	        }else{
	        	System.out.println("Es necesario especificar el argumento 'ficheroResultados' o 'r'");
            	System.exit(-2);
	        }
	        
	        
	        
	        if(cmdLn.getResult("ficheroNoEncontrados") != null){
	            if (cmdLn.getResult("ficheroNoEncontrados").getArgumentCount() > 0){
	                fileNotFound = cmdLn.getResult("ficheroNoEncontrados").getArgument();
	            }else{
	            	System.out.println("No se ha especificado el fichero de direcciones no encontradas");
	            	System.exit(-2);
	            }
	        }else{
	        	System.out.println("Es necesario especificar el argumento 'ficheroNoEncontrados' o 'n'");
            	System.exit(-2);
	        }
	        
	        if(cmdLn.getResult("ficheroLog") != null){
	            if (cmdLn.getResult("ficheroLog").getArgumentCount() > 0){
	            	fileCasosProducidos = cmdLn.getResult("ficheroLog").getArgument();
	            }
	        }
	        
	        if(cmdLn.getResult("separador") != null){
	            if (cmdLn.getResult("separador").getArgumentCount() > 0){
	            	separador = cmdLn.getResult("separador").getArgument();
	            }
	        }
	        
	        if(cmdLn.getResult("tieneCabecera") != null){
	            if (cmdLn.getResult("tieneCabecera").getArgumentCount() > 0){
	            	tieneCabecera = Integer.parseInt(cmdLn.getResult("tieneCabecera").getArgument()) == 1;
	            }
	        }
	        
	}
	

	public static void main(String[] args) throws IOException,
			URISyntaxException, InterruptedException {
		processCommandLineArguments(args);
		
		long t1 = System.currentTimeMillis();
		CallejeroService service;
		CallejeroServiceProxy proxy = new CallejeroServiceProxy(urlGeocoder);
		service = proxy.getCallejeroService();

		if (service != null) {
			// Para escribir los resultados geocodificados
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(fileResult), false)));
			
			// Para escribir los resultados no encontrados
			PrintWriter writer2 = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(fileNotFound), false)));
			
			// Para escribir un fichero con informacion adicional
			PrintWriter writer3 = new PrintWriter(new BufferedWriter(
					new FileWriter(new File(fileCasosProducidos), false)));
			

			String ficheroDirecciones = loadTextFile(filePath, "UTF-8");
			String[] direcciones = ficheroDirecciones.split("\n");
			for (int i = 0; i < direcciones.length; i++) {
					if (i == 0)// skip the header
					{	
						String[] fieldsStr = direcciones[i].split(separador);
						int length = fieldsStr.length;
						//escribimos la linea de la cabecera
						String line = "";
						for(int z = 0; z < length; z++){
							line += (z + ";");
						}
						line += length;
						line += ";";
						line += length + 1;
						line += ";";
						line += length + 2;
						line += ";";
						line += length + 3;
						line += ";";
						line += length + 4;
						line += ";";
						line += length + 5;
						line += ";";
						line += length + 6;
						
						writer.println(line);
						
						if(tieneCabecera)
							continue;
					}

					if (i % 50 == 0) {
						System.out.println("Procesados " + i);
						System.out.println("Segundos "
										+ ((double) System.currentTimeMillis() - (double) t1)
										/ 1000d);
						writer2.flush();
						writer.flush();
						writer3.flush();
					}
					
					try {
					String[] fieldsStr = direcciones[i].split(separador);

					// Normalizamos la direccion, ya que inicialmente en la
					// entrada de la misma se encuentra
					// el tipo de vias no normalizado, ademas de no seguir un
					// patron especifico las mismas
					String dirSinNormalizar = "";
					for(int j = 0; j < fields.length -1; j++){
						String text = fieldsStr[fields[j]];
						if(text.startsWith("\""))
							text = text.substring(1, text.length() -1);
						if(text.endsWith("\""))
							text = text.substring(0, text.length() -2);
						dirSinNormalizar += text+",";
					}
					String text = fieldsStr[fields[fields.length - 1]];
					if(text.startsWith("\""))
						text = text.substring(1, text.length() -1);
					if(text.endsWith("\""))
						text = text.substring(0, text.length() -2);
					dirSinNormalizar += text;
					
					
					Address dirNormalizada = service
							.normalizar(dirSinNormalizar);

					// Geocodificamos la direccion normalizada
					String tipoVia = dirNormalizada.getTipoVia();
					String nombreVia = dirNormalizada.getNombreVia();
					String numeroVia = dirNormalizada.getNumeroPortal();
					

					System.out.println("Direccion a buscar: "
							+ dirSinNormalizar);
					writer3.println("************************************************************************************************************");
					writer3.println("Direccion de entrada a buscar: "
							+ dirSinNormalizar);
					writer3.println("Direccion de entrada normalizada: " + tipoVia + " " + nombreVia+ " , " + numeroVia);
					// System.out.println("Direccion Normalizada: " + tipoVia +
					// " " + " " + nombreVia + ", " + numeroVia + " - " +
					// municipio + " / " + provincia);
					GeocoderResult[] result = null;

					
						if(codigoIne){
							String codigoIneStr = fieldsStr[campoCodigoIne];
							result = service.geocoderListSrs(nombreVia, numeroVia,
									tipoVia, codigoIneStr, projection);
						}else{
							String provincia = dirNormalizada.getProvincia();
							String municipio = dirNormalizada.getMunicipio();
							result = service.geocoderMunProvSrs(nombreVia, numeroVia,
									tipoVia, municipio, provincia, projection);

						}
					
					// resultMatch tendra el peso del resultado, en el rango
					// [0,1]
					double resultMatch = 0d;
					GeocoderResult bestResult = null;
					// Si solo obtenemos un resultado
					if (result.length == 1) {
						if(result[0].getMatchLevel().equalsIgnoreCase("NO MATCH"))
						{
							System.out.println("No hemos podido geocodificar "
									+ dirSinNormalizar);
							writer2.println(direcciones[i] + "%0%0%0");// x,y,matchLevel
						}
						if (result[0].getResultType().equalsIgnoreCase("exact")) {
							// Si el geocoder lo considera exacto, le damos la
							// maxima puntuacion
							resultMatch = 1;
						} else {
							// Se obtiene el peso de la respuesta
							resultMatch = ((GeocoderResult)result[0]).getSimilarity();
						}

						bestResult = result[0];

					} else if (result.length > 1) {
						// Si hay mas de un resultado, nos quedamos con el
						// exacto en caso de que lo haya,
						// o con el parcial con mayor peso en caso contrario
						double maxSimilarity = -1;
						for (GeocoderResult r : result) {
							// Se obtiene el peso de la respuesta
							double similarity = ((GeocoderResult)result[0]).getSimilarity();

							if (r.getResultType().equalsIgnoreCase("exact")) {
								maxSimilarity = 1;
								bestResult = r;
							}
							// fin mio
							if (similarity > maxSimilarity) {
								maxSimilarity = similarity;
								bestResult = r;
							}
						}
						resultMatch = maxSimilarity;
					}

					if (bestResult == null) {
						System.out.println("No hemos podido geocodificar "
								+ dirSinNormalizar);
						writer2.println(direcciones[i] + "%0%0%0");// x,y,matchLevel
					} else{
						
						String line = "";
						for(String fieldS:fieldsStr){
							fieldS = fieldS.replaceAll("\\n","");
							fieldS = fieldS.replaceAll("\\t","");
							fieldS = fieldS.replaceAll("\\r","");
							line += (fieldS + ";");
						}
						line += bestResult.getCoordinateX();
						line += ";";
						line += bestResult.getCoordinateY();
						line += ";";
						line += resultMatch;
						line += ";";
						line += bestResult.getStreetType();
						line += ";";
						line += bestResult.getStreetName();
						line += ";";
						line += bestResult.getStreetNumber();
						line += ";";
						line += bestResult.getLocality();
						
						writer.println(line);
							
						writer3.println("------------------------------------------------------------------------------------------------------------------");
						writer3.println("Vista de mayor peso:");
						writer3.println("http://mapea-sigc.juntadeandalucia.es/Componente/templateMapeaOL.jsp?wmcfile=http://mapea-sigc.juntadeandalucia.es/Componente/mapConfig/context_cdau_callejero_25830_no_cache.xml*Callejero,http://mapea-sigc.juntadeandalucia.es/Componente/mapConfig/context_cdau_satelite_25830_no_cache.xml*Satelite,http://mapea-sigc.juntadeandalucia.es/Componente/mapConfig/context_cdau_hibrido_25830_no_cache.xml*Híbrido&controls=panzoombar,navtoolbar,mouse,layerswitcher&projection="+ projection + "*" + units + "&center="
								+ bestResult.getCoordinateX() + ","
								+ bestResult.getCoordinateY()
								+ "&label=resultado&zoom=14");
					}
					
				} catch (Exception e) {
						System.out.println(e.toString());
						writer3.println(e.toString());
						continue;
				}	
			} // for
			writer.flush();
			writer2.flush();
			writer3.flush();
		} //if service != null
		
		
		long t2 = System.currentTimeMillis();
		double segundos = ((double) t2 - (double) t1) / 1000d;
		System.out.println("#########Proceso ejecutado en " + segundos+ ".############");

	}

	public static String toString(GeocoderResult result) {
		return result.getResultType() + "," + result.getMatchLevel() + ","
				+ result.getStreetType() + "," + result.getStreetName() + ","
				+ result.getStreetNumber() + "," + result.getLocality() + ","
				+ result.getCoordinateX() + "," + result.getCoordinateY();
	}

	public static String loadTextFile(String path) throws IOException,
			URISyntaxException {
		return loadTextFile(path, "UTF-8");
	}

	public static String loadTextFile(InputStream stream) throws IOException {
		return loadTextFile(stream, "UTF-8");
	}

	public static String loadTextFile(InputStream stream, String charset)
			throws IOException {

		String solution = "";
		BufferedReader buffer = new BufferedReader(new InputStreamReader(
				stream, charset));
		String line = null;
		while ((line = buffer.readLine()) != null) {
			solution += new String(line.getBytes(), "utf-8");
		}
		buffer.close();
		return solution;
	}

	public static String loadTextFile(String path, String charset)
			throws IOException, URISyntaxException {
		FileInputStream input = new FileInputStream(path);
		InputStreamReader in = new InputStreamReader(input, charset);

		StringBuffer fileData = new StringBuffer(1000);
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = in.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		in.close();
		input.close();
		return fileData.toString();
	}

	public static void createTextFile(String path, String content)
			throws IOException {
		FileOutputStream out = new FileOutputStream(path);
		out.write(content.getBytes());
	}

	public static String getPath(String fullPath) {
		int sep = fullPath.lastIndexOf(java.io.File.pathSeparatorChar);
		if (sep == -1)
			sep = fullPath.lastIndexOf("\\");
		if (sep == -1)
			sep = fullPath.lastIndexOf("/");
		if (sep == -1)
			return fullPath;
		return fullPath.substring(0, sep + 1);
	}

	public static String getFileName(String fullPath) {
		int dot = fullPath.lastIndexOf(".");
		int sep = fullPath.lastIndexOf(java.io.File.pathSeparatorChar);
		if (sep == -1)
			sep = fullPath.lastIndexOf("\\");
		return fullPath.substring(sep + 1, dot);
	}
}
