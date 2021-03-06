package gitrpg;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * DWRでJSから呼ばれるメソッドはすべてpublicでなければならない．また，必要なクラスはすべてdwr.xmlに定義されている必要がある．
 * @author Hiroshi
 *
 */
public class Mongo {

	public static void main(String[] args) throws Exception{
		Main.main();
		}


	public static String deleteResult() throws Exception{
		MongoClient mongoClient = new MongoClient();
		MongoDatabase database = mongoClient.getDatabase("mydb");
		MongoCollection<Document> col1 = database.getCollection("Result");
		deleteDatabase(col1);
		return "勝敗結果をリセットしました。";
	}

	public static void setDatabase1(MongoCollection<Document> col1,String reply) throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(reply);
		JSONArray json = (JSONArray)obj;

		for(Object obj0 : json){
			JSONObject tmp = (JSONObject)obj0;
			Document doc1 = Document.parse(tmp.toJSONString());
			col1.insertOne(doc1);
		}
	}

	public static void setDatabase2(MongoCollection<Document> col1,String reply) throws Exception {
		JSONParser parser = new JSONParser();
		Object raw = parser.parse(reply);
		JSONArray data = (JSONArray)raw;

		for(Object d : data){
			JSONObject json = (JSONObject)d;
			Document doc = Document.parse(json.toJSONString());
			col1.insertOne(doc);
		}
	}

	public static void deleteDatabase(MongoCollection<Document> col1) throws Exception {
		col1.deleteMany(new Document());
	}

	public static void deleteDatabase(MongoCollection<Document> col1,MongoCollection<Document> col2) throws Exception {
		col1.deleteMany(new Document());
		col2.deleteMany(new Document());
	}

	public static void deleteDatabase(MongoCollection<Document> col1,MongoCollection<Document> col2,MongoCollection<Document> col3) throws Exception {
		col1.deleteMany(new Document());
		col2.deleteMany(new Document());
		col3.deleteMany(new Document());
	}

	public static void fltrDatabase(MongoCollection<Document> col1,MongoCollection<Document> col2,String key1,String key2) throws Exception {
		BasicDBObject query = new BasicDBObject();
		Document doc1;
		query.put(key1, key2);
		MongoCursor<Document> cursor = col1.find(query).iterator();
		while (cursor.hasNext()) {
			doc1 = cursor.next();
			col2.insertOne(doc1);
		}
	}

	public static void getTime(MongoCollection<Document> col1,MongoCollection<Document> col2,String key,int DAY) throws Exception {
		BasicDBObject query = new BasicDBObject();
		Document doc1;
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		for(int i =0;i<=DAY;i++){
			String time = df.format(cal.getTime());
			cal.add(Calendar.DATE, -1);
			query.put(key, time);
			//System.out.println(time);
			MongoCursor<Document> cursor1 = col1.find(query).iterator();
			while (cursor1.hasNext()) {
				doc1 = cursor1.next();
				col2.insertOne(doc1);
			}
		}
	}

	public static String convString(MongoCollection<Document> col1) throws Exception {
		String doc="";
		FindIterable<Document> iterator = col1.find();
		MongoCursor<Document> cursor = iterator.iterator();
		while(cursor.hasNext()){
			doc += cursor.next();
			doc += System.getProperty("line.separator");
		}

		return doc;
	}


	public static String[] extractSha(String doc,int i) throws Exception {
		String regex = "comments, sha=........................................";
		int count=0;
		String strArray[] = new String[i+1];
		Pattern pa = Pattern.compile(regex);
		Matcher m1 = pa.matcher(doc);
		if (m1.find() ) {
			do {
				strArray[count++]=m1.group().substring(14, 54);
			} while (m1.find() );
			//System.out.println();
		}
		return strArray;
	}

	public static int[] extractInt (String reply,int j,String str)throws Exception {

		int intArray[] = new int[j*3+3];
		//System.out.println("j:"+j);
		//文字列データをオブジェクトに変換
		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);

		//とってきたデータが配列のときはJSONArrayにキャストする
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
			//System.out.println("change:"+(int)doc1);
			intArray[i]=(int)doc1;
		}
		return intArray;
	}

	/**
	 *
    JSONObject t1 = (JSONObject)commit.get("commit");
    JSONObject t2 = (JSONObject)t1.get("author");
    strArray[i] = (String)t2.get("name");
    System.out.println(strArray[i]);

    JSONObject t1 = (JSONObject)commit.get("stats");
    long doc1= (Long)t1.get("total");
    intArray[i]=(int)doc1;
	 **/

	public static String[] extractStr (String reply,int j,String str)throws Exception {

		String strArray[] = new String[j*3+3];
		System.out.println(reply);
		//文字列データをオブジェクトに変換
		JSONParser p = new JSONParser();
		Object parsed = p.parse(reply);
		int k=0;
		//とってきたデータが配列のときはJSONArrayにキャストする
		JSONArray array = (JSONArray)parsed;
		for(int i=0; i<array.size(); i++){

			JSONObject commit = (JSONObject)array.get(i);
			if(str=="Comment"){

				strArray[k++] = (String)commit.get("created_at");

				JSONObject t1 = (JSONObject)commit.get("user");
				strArray[k++] = (String)t1.get("login");

				strArray[k++] = (String)commit.get("body");

			} else if(str=="Member"){
				strArray[k] = (String)commit.get("login");
			}
		}
		System.out.println("でけた");
		return strArray;
	}

	public static int mongoCount(MongoCollection<Document> col){
		long count = col.count();
		return (int) count;
	}
}