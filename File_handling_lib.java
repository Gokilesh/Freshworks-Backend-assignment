import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;

public class File_handling_lib extends Thread {
	   
	public static int seconds = 0;
	public static String windows_path = "E:";
	public static String user_path = null;
	
	//insert function: to insert data into the datastore
	public static synchronized void insert(String path,String key,JSONObject jo) throws IOException
	{
		File file = new File(path);		
		String[] words=null;
		String s;
		FileReader fread = new FileReader(file); 
		BufferedReader br = new BufferedReader(fread);
		
		//To check whether the key and value pair is already present in the datastore or not
		while((s=br.readLine())!=null)  
	      {
	          words=s.split("-"); 
	          for (String word : words) 
	          {
	                 if (word.equals(key))   
	                 {
	                	  throw new IllegalArgumentException("The key"+" "+key+" "+"is already present");
	                 }
	          }
	      }
		br.close();
		FileWriter fr = new FileWriter(file, true);
		
		//To capped the key by 32 character and write it into the file
		if(key.length() < 32)
		{
			fr.write(key);
		}
		else
		{
			char[] keyarray = new char[key.length()];
			keyarray = key.toCharArray(); 
			for(int i=0;i<32;i++)
			{
				fr.write(keyarray[i]);
			}
		}
		fr.write('-');
		int bytesum =0;
		
		//To capped the key value pair at 16KB and write it into the file 
		for (Object keyss : jo.keySet())
		{
		        String keyStr = (String)keyss;
		        Object keyvalue = jo.get(keyStr);      
		        if (keyvalue.getClass() == Integer.class)
		        {	 
		        	   bytesum = bytesum + ((Integer) keyvalue).byteValue();	   
		        } 
		        else if (keyvalue.getClass() == String.class) 
		        {
		        	final byte[] utf16Bytes= ((String) keyvalue).getBytes("UTF-16BE");
		        	 bytesum = bytesum + utf16Bytes.length; 
		        }
		        		        
		        //if the json object is less than 16000 than it will write the object in the file else
		        //it will write upto 16000bytes and  it will leave the remains
		        if(bytesum < 16000)
	        	{
						        if (keyvalue.getClass() == Integer.class)
						        {
						        		    fr.write((String)keyStr);
						        		    fr.write(',');
						        		   	fr.write(String.valueOf(keyvalue));
						        		   	fr.write(',');
						        }
						        else if (keyvalue.getClass() == String.class) 
						        {
						        		 fr.write((String)keyStr);
						        		 fr.write(',');
						        		 fr.write((String)keyvalue);
						        		 fr.write(',');
						        }
	        	 }
	        	 else
	        	 {
	        		   break;
	        	 }
	    }
		fr.write('-');
		String linebreak = ""+System.lineSeparator();
		fr.write(linebreak);		
		try
		{
			fr.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{	
	}
	
	public static void Create(String path,String key,JSONObject jo,int seconds) throws IOException
	{
		//To check whether the file is exist or not, if not then create a new file
		//if the file already exist then the code will check the size of the file that should not be exceed 1GB
		user_path=null;
		String entire_path = path+"/datastore.txt";
		File file = new File(entire_path);
		boolean fvar;
		try 
		{
			fvar = file.createNewFile();
			if (fvar)
			{
				insert(entire_path,key,jo);	  
		    }
		    else
		    {
		    	 if(file.exists())
		    	 { 
		    		 int bytes = (int) file.length();
		    		 double GB = bytes/(1024*1024*1024);
		    		 if(GB<1.0)
		    		 {
		    			 //To insert data into the datastore
		    			 insert(entire_path,key,jo);	     
		    		 }
		    		 else
		    		 {
		    			 throw new OutOfMemoryError("data store size is exceeding 1GB");
		    		 }
		    	 }	 	
		     }
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//To achieve time to live property, thread has been created and make them sleep for certain time
		//Once the sleep time is over it will call auto delete function
		//autodelete funtion will delete the specific data from the data store
		File_handling_lib object = new File_handling_lib();
		object.start();
		try 
		{
			Thread.sleep(seconds);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		//To delete the key value pair once their time is expired
		if(seconds>0)
		{
			autodelete(entire_path,key);
		}
		
	}
	
	//To achieve optional parameter property, function overloading has been implemented
	public static void Create(String key,JSONObject jo) throws IOException
	{
		String entire_path = windows_path+"/datastore.txt";
		user_path = entire_path;
		File file = new File(entire_path);
		boolean fvar;
		try
		{
			fvar = file.createNewFile();
			if (fvar)
			{
				insert(entire_path,key,jo);	    
		    }
		    else
		    {
		    	 if(file.exists())
		    	 { 
		    		 int bytes = (int) file.length();
		    		 double GB = bytes/(1024*1024*1024);
		    		 if(GB<1.0)
		    		 {
		    			 insert(entire_path,key,jo);	  
		    		 }
		    		 else
		    		 {
		    			 throw new OutOfMemoryError("data store size is exceeding 1GB"); 
		    		 }
		    	 } 	
		     }
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		File_handling_lib object = new File_handling_lib();
		object.start();
		try 
		{
			Thread.sleep(seconds);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		if(seconds>0)
		{
			autodelete(entire_path,key);
		}
	}
	
	public static void Create(String path,String key,JSONObject jo) throws IOException
	{	
		user_path=null;
		String entire_path = path+"/datastore.txt";
		File file = new File(entire_path);
		boolean fvar;
		try
		{
			fvar = file.createNewFile();
			if (fvar)
			{
				insert(entire_path,key,jo);	   
		    }
		    else
		    {
		    	 if(file.exists())
		    	 { 
		    		 int bytes = (int) file.length();
		    		 double GB = bytes/(1024*1024*1024);
		    		 if(GB<1.0)
		    		 {
		    			 insert(entire_path,key,jo);	     
		    		 }
		    		 else
		    		 {
		    			 throw new OutOfMemoryError("data store size is exceeding 1GB"); 
		    		 }
		    	 }
		    	 	
		     }
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		File_handling_lib object = new File_handling_lib();
		object.start();
		try
		{
			Thread.sleep(seconds);
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		if(seconds>0)
		{
			autodelete(entire_path,key);
		}
	}
	
	public static void Create(String key,JSONObject jo,int seconds) throws IOException
	{			
		String entire_path = windows_path+"/datastore.txt";
		user_path = entire_path;
		File file = new File(entire_path);
		boolean fvar;
		try 
		{
			fvar = file.createNewFile();
			if (fvar)
			{
				insert(entire_path,key,jo);	     
		    }
		    else
		    {
		    	 if(file.exists())
		    	 { 
		    		 int bytes = (int) file.length();
		    		 double GB = bytes/(1024*1024*1024);
		    		 if(GB<1.0)
		    		 {
		    			 insert(entire_path,key,jo);	    
		    		 }
		    		 else
		    		 {
		    			 throw new OutOfMemoryError("data store size is exceeding 1GB");
		    		 }
		    	 }	
		     }
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		File_handling_lib object = new File_handling_lib();
		object.start();
		try
		{
			Thread.sleep(seconds);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		if(seconds>0)
		{
			autodelete(entire_path,key);
		}
	}
	
	//To get the json object corresponding to the key
	public static synchronized JSONObject Read(String key)
	{	
		JSONObject jo = new JSONObject(); 
		String path=null;
		if(user_path == null)
		{
			path = windows_path+"/"+"datastore.txt";
		}
		else
		{
			path = user_path;
		}
		File file = new File(path);
		String[] words=null;
		String[] jsonwords=null;
		String s;
		int flag=0;
		FileReader fread = null;
		try
		{
			fread = new FileReader(file);
		} 
		catch (FileNotFoundException e2) 
		{
			e2.printStackTrace();
		} 
		BufferedReader br = new BufferedReader(fread);
		
		//To fetch the value corresponding to the given key
		//To create a json object and return the json object
		try 
		{
			while((s=br.readLine())!=null)   
			  {
			     words=s.split("-");  
			     for(int i=0;i<words.length;i++)
			     {
			    	 if(words[i].equals(key))
			    	 {   flag = 1;
			    		 String jsonstring = words[i+1];
			    		 
			    		 jsonwords = jsonstring.split(",");
			    		 for(int j=0;j<jsonwords.length-1;j=j+2)
			    		 {
			    			 if(j%2==0)
			    			 {
			    				 jo.put(jsonwords[j],jsonwords[j+1]); 
			    			 }			 
			    		 }	 
			    	 }
			    	 if(flag == 1)
			    		 break;
			     }
			     if(flag == 1)
			    	 break;
			  }
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		try
		{
			br.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	return jo;
	}
		
	//To delete the specific data in the datastore
	public static synchronized void Delete(String key)
	{
		String path=null;
		if(user_path == null)
		{
			path = windows_path+"/"+"datastore.txt";
		}
		else
		{
			path = user_path;
		}
		File file = new File(path);
		int flag = 0;
		String[] words=null;
		String s;
		String jsonstring=null;
		FileReader fread = null;
		try 
		{
			fread = new FileReader(file);
		} 
		catch (FileNotFoundException e) 
		{			
			e.printStackTrace();
		} 
		BufferedReader br = new BufferedReader(fread);
		try 
		{
			//To find the target value in the file
			while((s=br.readLine())!=null)   
			  {
			     words=s.split("-");  
			    for(int i=0;i<words.length;i++) 
			    {
			    	if(words[i].equals(key))
			    	 {
			    		  jsonstring =words[i]+"-"+ words[i+1]+"-"; 
			    		  flag=1;
			    		  break;
			    	 }
			    }
			    if(flag == 1)
			    		break;
			  }
		}
		catch (IOException e) 
		{
			e.printStackTrace();	
		}
		try
		{
			br.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//To delete the data in the file
		//make the data in the file as a string and replaced the target value as a empty 
		//again write the string into the file
		String oldContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) 
            {
                oldContent = oldContent + line + System.lineSeparator();
                line = reader.readLine();
            }
            String newContent = oldContent.replaceAll(jsonstring+System.lineSeparator(), "");
            newContent.trim();
            writer = new FileWriter(file);
            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                reader.close();
                writer.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
	}   
      
    public static synchronized void autodelete(String path,String key)
    {
		File file = new File(path);
		int flag = 0;
		String[] words=null;
		String s;
		String jsonstring=null;
		FileReader fread = null;
		try 
		{
			fread = new FileReader(file);
		} 
		catch (FileNotFoundException e) 
		{			
			e.printStackTrace();
		} 
		BufferedReader br = new BufferedReader(fread);
		try {
			while((s=br.readLine())!=null)  
			  {
			    words=s.split("-");  
			    for(int i=0;i<words.length;i++) 
			    {
			    	if(words[i].equals(key))
			    	 {
			    		  jsonstring =words[i]+"-"+ words[i+1]+"-"; 
			    		  flag=1;
			    		 break;
			    	 }
			    }
			    if(flag == 1)
			    		break;
			  }
		}
		catch (Exception e) 
		{
		}
		try
		{
			br.close();
		}
		catch (Exception e) 
		{
		}
		String oldContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) 
            {
                oldContent = oldContent + line + System.lineSeparator();
                line = reader.readLine();
            }
            String newContent = oldContent.replaceAll(jsonstring+System.lineSeparator(), "");
            writer = new FileWriter(file);
            writer.write(newContent); 
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                reader.close();    
                writer.close();
            } 
            catch (Exception e) 
            {
            }
        }
	}
}
