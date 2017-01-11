package gitrpg;

import java.util.List;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * DWRでJSから呼ばれるメソッドはすべてpublicでなければならない．また，必要なクラスはすべてdwr.xmlに定義されている必要がある．
 * @author Hiroshi
 *
 */
public class Get {

	public static void main(String[] args) throws Exception{
		Main.main();
	}

	public static int countComment(String name) throws Exception{
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("mydb");
		MongoCollection<Document> col1 = database.getCollection("Comment");
		int result=0;
		for(Document doc : col1.find()){
			JSONObject json = new JSONObject();
			//json.put("id", doc.getString("id"));
			json.put("login", doc.getString("login"));
			if(doc.getString("login").equals(name)) result++;
		}
		System.out.println(result);
	return result;
	}

	public static int countCommit(String name) throws Exception{
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("mydb");
		MongoCollection<Document> col1 = database.getCollection("Commit");
		int result=0;
		for(Document doc : col1.find()){
			JSONObject json = new JSONObject();
			//json.put("id", doc.getString("id"));
			json.put("login", doc.getString("login"));
			if(doc.getString("login").equals(name)) result++;
			System.out.println(doc.getString("login"));
		}
		System.out.println(result);
	return result;
	}

	public static int sumChange(String name) throws Exception{
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("mydb");
		MongoCollection<Document> col1 = database.getCollection("Commit");
		int sum=0;
		JSONArray result = new JSONArray();

		for(Document doc : col1.find()){
			//String json = doc.toJson();
			JSONObject json = new JSONObject();
			json.put("name", doc.getString("name"));
			json.put("change", Integer.parseInt(doc.getString("change")));
			if(doc.getString("login").equals(name)) sum+=Integer.parseInt(doc.getString("change"));
		}
		return  sum;
	}

	public static String getPhoto(String name) throws Exception{
		String url = "https://api.github.com/users/" + name;
		String reply="["+http.apiGet(url)+"]";
		String avatar ="";
		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);
		JSONArray array = (JSONArray)parsed;

		for(int i=0; i<array.size(); i++){
		    JSONObject commit = (JSONObject)array.get(i);
		    avatar = (String)commit.get("avatar_url");
		}
		return avatar;
	}

	public static void getComment(String TEAM,String REPOS,
			MongoCollection<Document> col1) throws Exception {
		String url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/comments";
		String reply=http.apiGet(url);
		String a = "T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]Z\"},\"committer\":";
		String b = "\"},\"committer\":";
		reply = reply.replaceAll(a,b);

		Mongo.setDatabase1(col1, reply);
		int count = Mongo.mongoCount(col1);
		String strArray[] = new String[count*5+1];

		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);

		//とってきたデータが配列のときはJSONArrayにキャストする
		JSONArray array = (JSONArray)parsed;

		int k=0;
		for(int i=0; i<array.size(); i++){
			//JSONObjectにキャスト
			JSONObject commit = (JSONObject)array.get(i);

			JSONObject t1;

			strArray[k] = (String)commit.get("created_at");
			int su=strArray[k].length();
			strArray[k] = strArray[k].substring(0,su-1);
			k++;

			t1 = (JSONObject)commit.get("user");
			strArray[k++] = (String)t1.get("login");

			strArray[k++] = (String)commit.get("body");

		}
		Mongo.deleteDatabase(col1);

		int m=0;

		for(int j=0; j<array.size(); j++){
			Document doc = new Document();
			doc.append("created_at",strArray[m++]);
			doc.append("login",strArray[m++]);
			doc.append("body",strArray[m++]);
			col1.insertOne(doc);
		}

		//List<CommitData> result = new ArrayList<>();

		/*			CommitData data = new CommitData();
		data.setName(doc.getString("name"));
		data.setChange(doc.getString("change"));*/


//		JSONArray result = new JSONArray();
//
//		for(Document doc : col1.find()){
//			//String json = doc.toJson();
//			JSONObject json = new JSONObject();
//			json.put("name", doc.getString("name"));
//			json.put("change", Integer.parseInt(doc.getString("change")));
//			result.add(json);
//
//			System.out.println("名前: " + doc.getString("change"));
//
//		}
//		System.out.println(result.toJSONString());
	}



	public static void getBranch(String NAME,
			MongoCollection<Document> col1,
			MongoCollection<Document> col2) throws Exception {

		JSONArray result = new JSONArray();

		for(Document doc : col1.find()){
			JSONObject json = new JSONObject();
			//json.put("id", doc.getString("id"));
			json.put("name", doc.getString("login"));
			json.put("branch", doc.getString("ref"));
			if(doc.getString("id").equals("CreateEvent")) result.add(json);
		}
		Mongo.setDatabase1(col2, result.toJSONString());

	}

	public static void getEvent(String TEAM,String REPOS,String NAME,int DAY,
			MongoCollection<Document> col1) throws Exception {

		String url="https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/events"+"?page=1";
		int page = 1;
		String reply=http.apiGet(url);
		String replys[] = new String[11];
		String next="";
		String end="[]";
		int su = 0;
		page++;
		url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/events"+"?page="+page;
		replys[page]=http.apiGet(url);
		if(replys[page].equals(end)) {
		}else {
			su=reply.length();
			reply = reply.substring(0,su-1);
		}
		while(page!=10){
			url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/events"+"?page="+page;
			replys[page]=http.apiGet(url);
			if(replys[page].equals(end)) break;

			su=replys[page].length();
			replys[page] = replys[page].substring(1,su-1);
			reply=reply+replys[page];
			page++;
		}
		reply = reply + "]";

		String a = "T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]Z\"},\"committer\":";
		String b = "\"},\"committer\":";
		//reply = reply.replaceAll(a,b);
		Mongo.setDatabase1(col1, reply);
		int count = Mongo.mongoCount(col1);
		String strArray[] = new String[count*3+1];

		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);
		JSONArray array = (JSONArray)parsed;

		int k=0;
		for(int i=0; i<array.size(); i++){
			//JSONObjectにキャスト
			JSONObject commit = (JSONObject)array.get(i);

			JSONObject t1,t2,t3;

			strArray[k++] = (String)commit.get("type");
			t1 = (JSONObject)commit.get("actor");
			strArray[k++] = (String)t1.get("login");
			t1 = (JSONObject)commit.get("payload");
			strArray[k++] = (String)t1.get("ref");

		}
		Mongo.deleteDatabase(col1);

		int m=0;

		for(int j=0; j<array.size(); j++){
			Document doc = new Document();
			doc.append("id",strArray[m++]);
			doc.append("login",strArray[m++]);
			doc.append("ref",strArray[m++]);
			col1.insertOne(doc);
		}

	}
	public static List<CommitData> getCommit2(String TEAM,String REPOS,String NAME,int DAY,
			MongoCollection<Document> col1) throws Exception {
		String url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/commits";
		String reply=http.apiGet(url);
		String a = "T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]Z\"},\"committer\":";
		String b = "\"},\"committer\":";
		reply = reply.replaceAll(a,b);

		Mongo.setDatabase1(col1, reply);
		int count = Mongo.mongoCount(col1);
		String strArray[] = new String[count*5+1];

		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);

		//とってきたデータが配列のときはJSONArrayにキャストする
		JSONArray array = (JSONArray)parsed;

		int k=0;
		for(int i=0; i<array.size(); i++){
			//JSONObjectにキャスト
			JSONObject commit = (JSONObject)array.get(i);

			JSONObject t1,t2,t3;

			strArray[k++] = (String)commit.get("sha");
			String sha=strArray[k-1];
			t1 = (JSONObject)commit.get("commit");
			t2 = (JSONObject)t1.get("author");
			strArray[k++] = (String)t2.get("name");

			t1 = (JSONObject)commit.get("commit");
			t2 = (JSONObject)t1.get("author");
			strArray[k++] = (String)t2.get("date");

			String changeurl = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/commits/"+sha;

			strArray[k++] =String.valueOf(getChange2(changeurl));
			//strArray[k++] = "仮change";

			t1 = (JSONObject)commit.get("commit");
			strArray[k++] = (String)t1.get("message");
		}
		Mongo.deleteDatabase(col1);

		int m=0;

		for(int j=0; j<array.size(); j++){
			Document doc = new Document();
			doc.append("sha",strArray[m++]);
			doc.append("login",strArray[m++]);
			doc.append("date",strArray[m++]);
			doc.append("change",strArray[m++]);
			doc.append("message",strArray[m++]);
			col1.insertOne(doc);
		}

		//List<CommitData> result = new ArrayList<>();

		/*			CommitData data = new CommitData();
		data.setName(doc.getString("name"));
		data.setChange(doc.getString("change"));*/


		JSONArray result = new JSONArray();

		for(Document doc : col1.find()){
			//String json = doc.toJson();
			JSONObject json = new JSONObject();
			json.put("name", doc.getString("name"));
			json.put("change", Integer.parseInt(doc.getString("change")));
			result.add(json);

			System.out.println("名前: " + doc.getString("change"));

		}
		System.out.println(result.toJSONString());
		return  result;
	}


	public static int getChange2(String url) throws Exception {
		int j=0;
		String reply="["+http.apiGet(url)+"]";
		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);
		JSONArray array = (JSONArray)parsed;
		for(int i=0; i<array.size(); i++){
			//JSONObjectにキャスト
			JSONObject commit = (JSONObject)array.get(i);

			//shaを取り出し

			//JSONObject t1 = (JSONObject)commit.get("commit");
			//JSONObject t2 = (JSONObject)t1.get("author");
			//strArray[i] = (String)t2.get("name");
			//System.out.println(strArray[i]);

			//[]のやつのときはJson
			JSONObject t1 = (JSONObject)commit.get("stats");
			long doc1= (Long)t1.get("total");
			j=(int)doc1;
		}
		return j;
	}


	public static int getCommit(String TEAM,String REPOS,String NAME,int DAY,
			MongoCollection<Document> col1,
			MongoCollection<Document> col2,
			MongoCollection<Document> col3) throws Exception {
		String url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/commits";
		String reply=http.apiGet(url);
		String a = "T[0-9][0-9]:[0-9][0-9]:[0-9][0-9]Z\"},\"committer\":";
		String b = "\"},\"committer\":";
		reply = reply.replaceAll(a,b);
		Mongo.setDatabase1(col1, reply);
		int count2 = Mongo.mongoCount(col1);
		System.out.println(reply);
		System.out.println(count2);
		Mongo.fltrDatabase(col1,col2,"commit.author.name",NAME);
		Mongo.getTime(col2,col3,"commit.author.date",DAY);
		int count = Mongo.mongoCount(col3);

		return  count;
	}



	public static String[] getSha(MongoCollection<Document> col,int count) throws Exception {
		String sha[]=Mongo.extractSha(Mongo.convString(col),(int)count);
		for(int i=0;i<(int)count;i++){
			//System.out.println(sha[i]);
		}

		return sha;
	}

	public static void getChange(String TEAM,String REPOS,String sha[],
			MongoCollection<Document> col1) throws Exception {

		String url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/commits";
		String reply="[";
		String urls[]=new String[sha.length];
		for(int i=0;i<sha.length-1;i++){
			urls[i]=url+"/" + sha[i];
			reply = reply+ http.apiGet(urls[i]);
			//System.out.println(urls[i]);
		}
		reply = reply + "]";
		Mongo.setDatabase1(col1, reply);
		int count = Mongo.mongoCount(col1);
		System.out.println("数値:"+count);
		int intArray[] = new int[count+1];
		intArray = Mongo.extractInt(reply, count,"Change");
		for(int i=0;i<count;i++) System.out.println(intArray[i]);

	}

//	public static void getComment(String TEAM,String REPOS,
//			MongoCollection<Document> col1) throws Exception{
//		String url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/comments";
//		String reply=http.apiGet(url);
//		Mongo.setDatabase1(col1, reply);
//		int count = Mongo.mongoCount(col1);
//		String strArray[] = new String[count*3];
//		strArray =Mongo.extractStr(reply, count,"Comment");
//		for(int i=0;i<count*3;i++) System.out.println("こめのｔ"+strArray[i]);
//
//	}

	public static void getMember(String TEAM,String REPOS,
			MongoCollection<Document> col1) throws Exception{
		String url = "https://api.github.com/repos/" +TEAM+"/"+ REPOS +"/contributors";
		String reply=http.apiGet(url);
		Mongo.setDatabase1(col1, reply);
		int count = Mongo.mongoCount(col1);
		String strArray[] = new String[count*3+3];
		strArray =Mongo.extractStr(reply, count,"Member");
		for(int i=0;i<count;i++) System.out.println("メンバ"+strArray[i]);;

	}

    public static String helloWorld(String name){
    	return name + ":HelloWorld";

    }
}