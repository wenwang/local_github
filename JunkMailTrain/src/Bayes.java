import org.apache.lucene.analysis.Token;   
import org.apache.lucene.analysis.cn.*;   
   
import java.util.*;   
import java.io.*;   
   
public class Bayes    
{   
     private Hashtable<String, Double> badHash;   
     private Hashtable<String, Double> goodHash;   
     private Hashtable<String, Double> probabilityHash = new Hashtable<String, Double>();   
     //初始化   
     public void init()throws Exception   
     {   
         badHash = new Hashtable<String, Double>();   
         goodHash = new Hashtable<String, Double>();   
         buildEmailHash("D:\\workspace\\BadMail\\train\\badEmail.txt",badHash);   
         buildEmailHash("D:\\workspace\\BadMail\\train\\goodEmail.txt",goodHash);   
         buildNewHashTable();   
           
     }   
     //创建垃圾邮件哈希表   
     public void buildEmailHash(String path, Hashtable<String, Double> table) throws Exception   
     {   
         String badInput = readFile(path);   
         ChineseTokenizer tokenizer = new ChineseTokenizer(   
                                       new StringReader(badInput));   
         Token token;   
         Hashtable<String, Integer> tempHash = new Hashtable<String, Integer>();   
         double rate = 0.0;   
         int total = 0;   
         while ((token = tokenizer.next()) != null)   
         {   
             total++;   
             String temp = token.termText();   
             if(table.containsKey(temp))   
             {   
                 int counter = (Integer)tempHash.get(temp);   
                 counter++;   
                 tempHash.remove(temp);   
                 tempHash.put(temp, counter);   
             }   
             else   
             {   
                 tempHash.put(temp, 1);   
             }   
         }   
         //将垃圾邮件中字符及其概率放入badhash中   
         for(Iterator<String> it = tempHash.keySet().iterator(); it.hasNext();)    
         {      
             String key = (String)it.next();   
             rate = (double)tempHash.get(key)/total;     
             table.put(key, rate);    
             //System.err.println(key+":"+tempHash.get(key)+":"+total);
         }   
     }   
     /*创建新的probability Hash表，其中的概率表示  
     在邮件中出现 TOKEN 串 ti 时，该邮件为垃圾邮件的概率  
     */   
     public void buildNewHashTable()   
     {   
        for(Iterator it = badHash.keySet().iterator(); it.hasNext();)    
         {      
             String key = (String)it.next(); 
             //System.out.println(key+":"+badHash.get(key));
             
             // P （ A|ti ） =P2 （ ti ） /[ （ P1 （ ti ） +P2 （ ti ） ]    
             double badRate = (Double)badHash.get(key);   
             double allRate = badRate;   
             if(goodHash.containsKey(key))   
             {   
                 allRate  += (Double)goodHash.get(key);    
             }   
             
             double probability=badRate/allRate;
             probabilityHash.put(key, probability); 
             System.out.println(key+"-"+badRate/allRate);
         }   
     }   
   
     //读文件   
     public String readFile(String path)throws Exception   
     {   
         BufferedReader br = new BufferedReader(   
                 new FileReader(path));   
         String str = "";   
         while(true)   
         {   
             if(br.readLine() == null) break;   
             str += br.readLine();   
         }   
         br.close();   
         return str;   
     }   
     //判断是否为垃圾邮件 返回true表示非垃圾邮件 返回false表示是垃圾邮件   
     public boolean judgeEmail(String email, double weight) throws Exception   
     {   
         //P(A|t1 ,t2, t3……tn)=（P1*P2*……PN）/[P1*P2*……PN+（1-P1）*（1-P2）*……（1-PN）]    
         ChineseTokenizer tokenizer = new ChineseTokenizer(   
                                       new StringReader(email));   
         Token token;   
         boolean flag = true;   
         double rate = 1.0;   
         double tempRate = 1.0;   
         double finalRate = 0.0;   
         while ((token = tokenizer.next()) != null)   
         {   
             String key = token.termText();   
             if(!probabilityHash.containsKey(key))   
             {   
                 continue;   
             }   
             else   
             {   
                 double temp = (Double)probabilityHash.get(key);   
                 tempRate *= 1 - temp;   
                 rate *= temp;   
             }   
         }   
         finalRate = rate/(rate + tempRate);   
         if(finalRate > weight) flag = false;   
         return flag;   
     }   
     public static void main(String args[]) throws Exception   
     {   
         Bayes bayes = new Bayes();   
         bayes.init();   
         String email ="苏州乐园                  主题乐园 1   一票制(含包车费和门票费) 150 160";   
         double weight = 0.5;   
         if(bayes.judgeEmail(email, weight))   
         {   
             System.out.println("It is OK!");   
         }   
         else   
         {   
             System.out.println("It is wrong!");   
         }   
     }   
   
}   