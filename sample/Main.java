import java.io.IOException;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class Main {
	
	public static void main(String[] args) throws IOException{
		Scanner in = new Scanner(System.in);
		System.out.println("Who do you want to stalk?");
		String stalk = in.nextLine();
		stalk.replace(" ", "+");
		Document doc = Jsoup.connect("https://www.cornell.edu/search/people.cfm?q="+stalk).get();
		Elements idk = doc.select("#peoplename");
		String name = idk.text();
		
		Elements prof = doc.select("#peopleprofile");
		Elements emp = prof.select("#employment1");
		Elements typ = prof.select("#generalinfo");
		String netid = typ.text();
		int dex = netid.indexOf("NETID");
		int dex2 = netid.indexOf("EMAIL");
		String mail = netid.substring(dex2, netid.length());
		netid = netid.substring(dex, dex2);
		String college = emp.text();
		
		name = name.substring(0, name.length() - 6);
		System.out.println(name);
		System.out.println(netid);
		System.out.println(mail);
		System.out.println(college);
		System.out.println("Done.");
		
		in.close();
	}

}
