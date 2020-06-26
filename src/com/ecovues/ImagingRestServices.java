package com.ecovues;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import oracle.imaging.BasicUserToken;
import oracle.imaging.DocumentContentService;
import oracle.imaging.ImagingException;
import oracle.imaging.NameId;
import oracle.imaging.Search;
import oracle.imaging.SearchArgument;
import oracle.imaging.SearchParameter;
import oracle.imaging.SearchParameters;
import oracle.imaging.SearchService;
import oracle.imaging.SearchValue;
import oracle.imaging.ServicesFactory;
import oracle.imaging.TypedValue;
import oracle.imaging.UserToken;

import org.apache.log4j.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.sql2o.tools.NamedParameterStatement;


@Path("documents")
public class ImagingRestServices {
    public ImagingRestServices() {
    }
    private static final Logger logger = Logger.getLogger(ImagingRestServices.class);

    
    @GET
    @Path("/validate")
    public String getName() {
        return "Rest service working fine";
    }
    
        public ConfigData configs() throws Exception {
            String username="";
            String password="";
            String imagingUrl="";
            String migrationPath="";
            Connection ebsconn = JdbcHelper.getJDBCConnectionFromDataSource(true);
            
            ConfigData config = new ConfigData(username,password,imagingUrl, migrationPath);
            String query = "select * from ecoui_configs";//xxcv_img_configs


            NamedParameterStatement p = new NamedParameterStatement(ebsconn, query, true);

            ResultSet rs = null;

            try {
                rs = p.executeQuery();
               
                while(rs.next()){  
              
                    
               if(rs.getString("configs_name").equalsIgnoreCase("ImagingUrl"))
                imagingUrl =  rs.getString("configs_value");
                                   
                                   if(rs.getString("configs_name").equalsIgnoreCase("ImagingUsername"))
                                      username= rs.getString("configs_value");
                                   
                                   if(rs.getString("configs_name").equalsIgnoreCase("ImagingPassword"))
                                      password = rs.getString("configs_value");
                    
                    if(rs.getString("configs_name").equalsIgnoreCase("MigrationPath"))
                       migrationPath = rs.getString("configs_value");
                    
                     config = new ConfigData(username,password,imagingUrl,migrationPath);
                }
                p.close();
                rs.close();
            } catch (SQLException e) {
                
                
            } finally {
                
                ebsconn.close();
            }


            return config;
        }
    
   
            public File downloadDocument(String migrationPath,String documentId, String username, String password, String imagingUrl) throws Exception {
            
              File file = File.createTempFile("temp", null);
            
                  String documentName="";
            
                  InputStream inputStream = null;
                 

                  try {
                      DataHandler fileData = null;
            
                      UserToken credentials = new BasicUserToken(username, password);
                      
                      ServicesFactory servicesFactory =
                          ServicesFactory.login(credentials, Locale.US, imagingUrl+"/imaging/ws");
                      
                      try {
                          DocumentContentService docContentService = servicesFactory.getDocumentContentService();
            
                          fileData = docContentService.retrieve(documentId);
            
                          documentName = fileData.getName();
            
                          
                         
                          String fileType = fileData.getContentType();
                                fileType=   fileType.substring(fileType.indexOf("/")+1);
                          logger.info("File type: "+fileType);
                          if(fileType.equals("plain"))
                              fileType="txt";
                          
                          File docFile = new File(migrationPath+"/"+documentId+"."+fileType);
                          
                          
                          inputStream = fileData.getInputStream();
                          
                          try (FileOutputStream outputStream = new FileOutputStream(docFile)) {

                              int read;
                              byte[] bytes = new byte[1024];

                              while ((read = inputStream.read(bytes)) != -1) {
                                  outputStream.write(bytes, 0, read);
                              }
                              logger.info("Document written to : "+migrationPath);
                          
                          }
                          catch(IOException e){
                              logger.info("Error while retrieving document: "+e.getMessage());
                              file.delete();
                              
                          }
                        
                          
                          file.delete();
                          return docFile;

                      } catch (IOException e) {
                      } finally {
                          if (servicesFactory != null) {
                              servicesFactory.logout();
                          }
                      }
                  } catch (Exception e) {
                      file.delete();

                  }
            
            
             return file;
            
          }
         
    
    class ConfigData {
        String userName, password, imagingUrl, migrationPath;

        public ConfigData(String userName, String password, String imagingUrl, String migrationPath) {
            this.userName = userName;
            this.password = password;
            this.imagingUrl = imagingUrl;
            this.migrationPath= migrationPath;
        }
    }
    
    @POST
          @Path("/exportDocuments")
           @Consumes("application/json")
            public Response getDocument(String getDocPayload) throws Exception {
//                String current = new java.io.File( "." ).getCanonicalPath();
//                       System.out.println("Current dir:"+current);
//                String currentDir = System.getProperty("user.dir");
//                       System.out.println("Current dir using System:" +currentDir);
               String username   = "";
               String password   = "";
               String imagingUrl = "";
               String migrationPath="";
               
                JSONObject output=new JSONObject();
                String encryptedData="";
                String status="Success";
                String errorMessage="";
                int noOfDocs=0;
                
        try{
                ConfigData config = configs();
                
                 username   = config.userName;
                 password   = config.password;
                 imagingUrl = config.imagingUrl;
                 migrationPath = config.migrationPath;
                // migrationPath = "C:\\ecovue-dashboard\\Imaging files";
            
//            imagingUrl = "http://wcctimg2.mountaire.net:16000";
//            username="weblogic";
//            password="ecovue1$";
         //   migrationPath="/home/appimgq/ImportMountInvoices";
        }
        catch(Exception e) {
            output.put("Status", "Failed");
            output.put("Error Message", "Error while fetching Imagin url and Credentials from DB");
            return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                            "*").header("Access-Control-Allow-Methods",
                                                                                        "GET, POST, DELETE, PUT").build();
           
        }
                FileOutputStream fos = null;
                ZipOutputStream zipOS = null;
                try {
                    fos = new FileOutputStream(new File("ImagingAPI.zip"));
                } catch (FileNotFoundException e) {
                }
                zipOS = new ZipOutputStream(fos);
                
                                JSONParser parser = new JSONParser();
				JSONObject getDocumentObj=null;
				try {
					getDocumentObj = (JSONObject) parser.parse(getDocPayload);
				} catch (ParseException e) {
				}
                                
                            JSONArray paramsArr = (JSONArray)getDocumentObj.get("parameters");
                            Iterator<org.json.simple.JSONObject> paramIterator = paramsArr.iterator();
                                        
                                        List<String> paramsList =  new ArrayList<String>();
                           
                            for (int i = 0; paramIterator.hasNext(); i++) {
                                org.json.simple.JSONObject paramObj = (org.json.simple.JSONObject)paramIterator.next();
                                 paramsList.add((String)paramObj.get("param_name"));
                            }


				String searchType=(String)getDocumentObj.get("search_name");
                               // String encryptedFlag=(String)getDocumentObj.get("encrypted_output");
                String documentId="";
                List<String> docIdList = new ArrayList<String>();
                try {
                    UserToken credentials = new BasicUserToken(username, password);
                    ServicesFactory servicesFactory =
                        ServicesFactory.login(credentials, Locale.US, imagingUrl+"/imaging/ws");
                    
                    try{
                                      SearchService searchService = servicesFactory.getSearchService();
                                      NameId invoiceSearchNameId = null;
                                      List<NameId> searchList = 
                                         searchService.listSearches(Search.Ability.VIEW);
                        
                                      for (NameId nameId: searchList) {
                                         if (nameId.getName().equals(searchType)) {
                                            invoiceSearchNameId = nameId;
                                         }
                                      }

                                      if (invoiceSearchNameId == null) {
                                        output.put("Status", "Failed");
                                        output.put("Error Message", "Search name: "+searchType+" is not found");
                                        return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                        "*").header("Access-Control-Allow-Methods",
                                                                                                                    "GET, POST, DELETE, PUT").build();
                                        
                                      }
                        
                                      SearchParameters searchParams= searchService.getSearchParameters(invoiceSearchNameId);
                                       java.util.List<SearchParameter> searchparamsList =searchParams.getSearchParameters();
                                       List<String> actualparamsList =  new ArrayList<String>();
                        
                                           for(SearchParameter searchId : searchparamsList){
                                                actualparamsList.add(searchId.getParameterName());
                                            String operatorText = searchId.getDefaultOperator().toString();
                                             logger.info("operatorText: "+operatorText);
                                               logger.info("ParameterName: "+searchId.getParameterName());
                                             if(searchId.isRequired())
                                               {
                                                if(!paramsList.contains(searchId.getParameterName()))
                                                {
                                                    output.put("Status", "Failed");
                                                    output.put("Error Message", searchId.getParameterName()+" is mandatory");
                                                    return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                                    "*").header("Access-Control-Allow-Methods",
                                                                                                                                "GET, POST, DELETE, PUT").build();
                                                   
                                                }
                                               }
                                                
                                               
                                           
                                           }
                        
                                       for(String paramId : paramsList){
                                           
                                           if(!actualparamsList.contains(paramId))
                                           {
                                               output.put("Status", "Failed");
                                               output.put("Error Message", paramId+" doesn't exist in search List of "+searchType);
                                               return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                               "*").header("Access-Control-Allow-Methods",
                                                                                                                           "GET, POST, DELETE, PUT").build();
                                           }    
                                               
                                            
                                           
                                       }
                        
                                       List<SearchArgument> searchArguments =
                                          new ArrayList<SearchArgument>();
                        
                                       JSONArray paramsArr1 = (JSONArray)getDocumentObj.get("parameters");
                                       Iterator<org.json.simple.JSONObject> paramIterator1 = paramsArr1.iterator();
                                    
                                       String colValuesFileName=""; 
                                       for (int i = 0; paramIterator1.hasNext(); i++) 
                                               {
                                                   org.json.simple.JSONObject paramObj = (org.json.simple.JSONObject)paramIterator1.next();
                                                   String paramname=(String)paramObj.get("param_name");
                                                   String paramvalue = (String)paramObj.get("param_value");
                                                   colValuesFileName = colValuesFileName+paramvalue.replace("/","-")+"_TO_"; 
                                           
                                                   if(paramname.equals("Document Creation Date"))
                                                   {
                                                   SearchValue transactionNumVal = new SearchValue(SearchValue.Type.DATE, new SimpleDateFormat("yyyy-MM-dd").parse((String)paramObj.get("param_value")));
                                                   SearchArgument transactionArg =  new SearchArgument((String)paramObj.get("param_name"), transactionNumVal);
                                                   transactionArg.setOperatorValue(Search.Operator.GREATER_THAN_OR_EQUAL);
                                                   
                                                       if((String)paramObj.get("param_value")!=null)        
                                                       searchArguments.add(transactionArg);
                                                   }
                                                   else if(paramname.equals("Document Creation Date 1"))
                                                   {
                                                   SearchValue transactionNumVal = new SearchValue(SearchValue.Type.DATE, new SimpleDateFormat("yyyy-MM-dd").parse((String)paramObj.get("param_value")));
                                                   SearchArgument transactionArg =  new SearchArgument((String)paramObj.get("param_name"), transactionNumVal);
                                                   transactionArg.setOperatorValue(Search.Operator.LESS_THAN_OR_EQUAL);
                                                   
                                                       if((String)paramObj.get("param_value")!=null)        
                                                       searchArguments.add(transactionArg);
                                                   }
                                                  else {
//                                           SearchValue transactionNumVal = new SearchValue(SearchValue.Type.DATE, new SimpleDateFormat("dd/MM/yyyy").parse((String)paramObj.get("param_value")));
//                                           SearchArgument transactionArg =  new SearchArgument((String)paramObj.get("param_name"), transactionNumVal);
//                                           transactionArg.setOperatorValue(Search.Operator.LESS_THAN_OR_EQUAL);
//                                           
//                                               if((String)paramObj.get("param_value")!=null)        
//                                               searchArguments.add(transactionArg);
                                                       SearchValue transactionNumVal = new SearchValue(SearchValue.Type.TEXT, (String)paramObj.get("param_value"));
                                                       SearchArgument transactionArg =  new SearchArgument((String)paramObj.get("param_name"), transactionNumVal);
                                                       transactionArg.setOperatorValue(Search.Operator.EQUAL_TEXT);
                                                       
                                                           if((String)paramObj.get("param_value")!=null)        
                                                           searchArguments.add(transactionArg);
                                                   }
                                           
                                                   
                                                                                  
                                                                                 
                                                                                  
                                             
                                       }
				
                                      Search.ResultSet resultSet = searchService.executeSavedSearch(invoiceSearchNameId,searchArguments);
                        
                                       colValuesFileName=colValuesFileName.substring(0,colValuesFileName.length()-4);
                        
                                    
                        
                                       try{    
                                                

                                                  for (Search.Result row: resultSet.getResults()) {
                                                      
                                                      documentId = row.getDocument().getId();
                                                      
                                                      downloadDocument(migrationPath, documentId, username, password, imagingUrl);
                                                      noOfDocs++;
                                                      
            
                                                  }
                                           
                                             
                                       }catch(Exception e){
                                           output.put("Error Message", e.getMessage());
                                               return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                               "*").header("Access-Control-Allow-Methods",
                                                                                                                           "GET, POST, DELETE, PUT").build();
                                               }  
                        
                                       try{    
                                                  FileWriter fw=new FileWriter(migrationPath+"/"+"ImagingMigration_"+colValuesFileName+"_Column_Vals.txt");    
                                                 
                                                
                                                

                                                  for (Search.Result row: resultSet.getResults()) {
                                                      documentId = row.getDocument().getId();
                                                      
                                                      DocumentContentService docContentService = servicesFactory.getDocumentContentService();
                                                      DataHandler fileData = null;
                                                      
                                                      
                                                      fileData = docContentService.retrieve(documentId);
                                                      
                                                      String fileType = fileData.getContentType();
                                                            fileType=   fileType.substring(fileType.indexOf("/")+1);
                                                      
                                                      
                                                     
                                                                            fw.write(migrationPath+"/"+documentId+"."+fileType+"|"); 
                                                      
                                                      
                                                                            for (TypedValue typedValue: row.getColumnValues()) {
                                                                                
                                                                                logger.info("Attribute value: "+typedValue.getValue());
                                                                                
                                                                               // System.out.println(typedValue.getStringValue());
                                                                                
                                                                                String sDate1=typedValue.getStringValue();
                                                                                
                                                                                logger.info("Length value: "+sDate1.length());
                                                                                
                                                                                if(sDate1.contains("-")&&!sDate1.contains(":")&&sDate1.length()<20) {
                                                                                    
                                                                                    //String sDate1=typedValue.getStringValue();
                                                                                    Date date1=new SimpleDateFormat("yyyy-mm-dd").parse(sDate1);
                                                                                    SimpleDateFormat formatter = new SimpleDateFormat("mm/dd/yyyy");
                                                                                    String strDate= formatter.format(date1);
                                                                                    
                                                                                    fw.write(strDate + "|");
                                                                                    
                                                                                    
                                                                                }
                                                                                else if(!sDate1.contains(":"))
                                                                                {
                                                                                    logger.info("sDate1: "+sDate1);
                                                                                    if(sDate1.equals("OFR"))
                                                                                        sDate1 = "OFR_11g";
                                                                                fw.write(sDate1 + "|");    
                                                                                }
                                                                            }
                                                      fw.write("\n");
                                       
                                                  }
                                           
                                           logger.info("File written to "+migrationPath);
                                           fw.close();    
                                       }catch(Exception e){
                                                output.put("Error Message", e.getMessage());
                                               return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                               "*").header("Access-Control-Allow-Methods",
                                                                                                                           "GET, POST, DELETE, PUT").build();
                                               }  
                        
                                                
                                       
                                   }
                                    catch (Exception e) {
                                        output.put("Status", "Failed");
                                        output.put("Error Message", e.getMessage());
                                        return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                        "*").header("Access-Control-Allow-Methods",
                                                                                                                    "GET, POST, DELETE, PUT").build();
                                    }
                                   finally {
                                      if (servicesFactory != null) {
                                         servicesFactory.logout();
                                      }
                                   }
                                }
                                catch (ImagingException e) {
                                    output.put("Status", "Failed");
                                    output.put("Error Message", e.getMessage());
                                    return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                                                    "*").header("Access-Control-Allow-Methods",
                                                                                                                "GET, POST, DELETE, PUT").build();
                                }

                        for(int i=0; i<docIdList.size();i++) {
                           // writeToZipFile(docIdList.get(i)+".pdf", downloadInputStream(docIdList.get(i)+".pdf",documentId, username, password, imagingUrl), zipOS);
                           // logger.info("docIdList.get(i): "+docIdList.get(i));
                            downloadDocument(migrationPath, docIdList.get(i), username, password, imagingUrl);
                            
                            
                        }
                       
                        output.put("Status", "Success");
                        output.put("Error Message", String.valueOf(noOfDocs)+" Documents' successfully moved to "+migrationPath);
                   
              return Response.status(200).entity(output.toString()).header("Access-Control-Allow-Origin",
                                                                              "*").header("Access-Control-Allow-Methods",
                                                                                          "GET, POST, DELETE, PUT").build();

            }   
}   