package com.puuga.exontologyandroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class Ontology {
	
	ArrayList<SearchProperty> arrSearchProperty;
	ArrayList<Integer> removeIndex;
	
	public String makeQueryFromJSON(String json) {
		System.out.println("makeQueryFromJSON");
		Gson gson = new Gson();
		SearchProperty[] searchPropertiesFromJSON = gson.fromJson(json, SearchProperty[].class);
		
		// check 
		System.out.println("searchPropertiesFromJSON.length"+searchPropertiesFromJSON.length);
		for (SearchProperty searchProperty : searchPropertiesFromJSON) {
		    System.out.println(searchProperty);
		}
		
		//
		arrSearchProperty = new ArrayList<SearchProperty>();
		removeIndex = new ArrayList<Integer>();

		int counter = 1;

		Object[] possibleLanguageValues = { "TH", "EN", "TH&EN" };

		Object[] possibleValues = { "flowers", "imageOfProvince", "motto",
				"nameOfProvince", "seals", "traditionalNameOfProvince", "tree",
				"URLOfProvince" };
		Object[] possibleKeywords = { "hasFlowers", "hasImageOfProvince",
				"hasMotto", "hasNameOfProvince", "hasSeals",
				"hasTraditionalNameOfProvince", "hasTree", "hasURLOfProvince" };
		
		for(int i=0; i<searchPropertiesFromJSON.length; i++) {
			System.out.println("option " + counter);
			// Choose property to display
			Object selectedValue = searchPropertiesFromJSON[i].getLabel();
			Object selectedLanguageValue = null;
			
			// Select language to display
			if (selectedValue.toString().equals("imageOfProvince")
					|| selectedValue.toString().equals("URLOfProvince")) {

			} else {
				selectedLanguageValue = searchPropertiesFromJSON[i].getLanguage();
			}

			String inputValue = searchPropertiesFromJSON[i].getKeyword();
			int selectedValueIndex = Arrays.binarySearch(possibleValues,
					selectedValue);
			if (selectedValue.equals(possibleValues[0])
					|| selectedValue.equals(possibleValues[2])
					|| selectedValue.equals(possibleValues[3])
					|| selectedValue.equals(possibleValues[4])
					|| selectedValue.equals(possibleValues[5])
					|| selectedValue.equals(possibleValues[6])) {
				SearchProperty searchProperty = new SearchProperty(
						selectedValue.toString() + counter,
						possibleKeywords[selectedValueIndex].toString());
				searchProperty.setSelect(true);
				searchProperty.setKeyword(inputValue);
				searchProperty.setLanguage(selectedLanguageValue.toString());
				arrSearchProperty.add(searchProperty);
				if (selectedLanguageValue.equals(possibleLanguageValues[2])) {
					counter++;
					SearchProperty searchProperty2 = new SearchProperty(
							selectedValue.toString() + counter,
							possibleKeywords[selectedValueIndex].toString());
					searchProperty2.setSelect(true);
					searchProperty2.setLanguage(selectedLanguageValue
							.toString());
					arrSearchProperty.add(searchProperty2);
				}
			} else if (selectedValue.equals(possibleValues[1])
					|| selectedValue.equals(possibleValues[7])) {
				SearchProperty searchProperty = new SearchProperty(
						selectedValue.toString() + counter,
						possibleKeywords[selectedValueIndex].toString());
				searchProperty.setSelect(true);
				searchProperty.setKeyword(inputValue);
				arrSearchProperty.add(searchProperty);
			}


			System.out.print(" label:"
					+ arrSearchProperty.get(counter - 1).getLabel());
			System.out.print(" language:"
					+ arrSearchProperty.get(counter - 1).getLanguage());
			System.out.print(", nameInDB :"
					+ arrSearchProperty.get(counter - 1).getNameInDB());
			System.out.print(", keyword:"
					+ arrSearchProperty.get(counter - 1).getKeyword());
			System.out.println();
			counter++;
		}
		
		String q = "PREFIX myont: <http://www.owl-ontologies.com/tourism.owl#> "
				+ "PREFIX xsp: <http://www.owl-ontologies.com/2005/08/07/xsp.owl#> "
				+ "PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#> "
				+ "PREFIX swrl: <http://www.w3.org/2003/11/swrl#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "SELECT ";
		for (int i = 0; i < arrSearchProperty.size(); i++) {
			String label = arrSearchProperty.get(i).getLabel();
			String lableMNow = label.substring(0, label.length() - 1);
			String lableMNext = null;
			String labelNext = null;
			try {
				labelNext = arrSearchProperty.get(i + 1).getLabel();
				lableMNext = labelNext.substring(0, labelNext.length() - 1);
			} catch (Exception e) {
			}

			if (lableMNow.equals(lableMNext)) {
				removeIndex.add(i);
				continue;
			}
			q += "?" + label + " ";
		}
		q += " " + "WHERE {?a ";

		for (int i = 0; i < arrSearchProperty.size(); i++) {
			String nameInDB = arrSearchProperty.get(i).getNameInDB();
			String label = arrSearchProperty.get(i).getLabel();
			q += "myont:" + nameInDB + " ?" + label + "; ";
		}
		q += " ";

		// setup language
		for (int i = 0; i < arrSearchProperty.size(); i++) {
			String language = arrSearchProperty.get(i).getLanguage();
			String label = arrSearchProperty.get(i).getLabel();
			String labelM = label.substring(0, label.length() - 1);
			if (labelM.equals("imageOfProvince")
					|| labelM.equals("URLOfProvince")) {
				continue;
			} else if (language.equals("TH&EN")) {
				continue;
			}
			q += ". FILTER langMatches( lang(?" + label + "), '" + language
					+ "') ";
		}
		// setup keyword
		for (int i = 0; i < arrSearchProperty.size(); i++) {
			String label = arrSearchProperty.get(i).getLabel();
			String keyword = arrSearchProperty.get(i).getKeyword();
			if (keyword != null) {
				q += ". FILTER (REGEX(str(?" + label + "),'(^|\\\\W)"
						+ keyword + "','i'))";
			}
		}

		q += "}";

		//

		System.out.println(q);
		
		return q;
	}
	
	public String getResultInJSON(String queryQ, InputStream inputFile){
		System.out.println("getResultInJSON");
		
		ArrayList<String> dataForTHead = null;
		ArrayList<String[]> dataForTBody = null;
		
		//
		try {
			String inputFileName = "file:///android_asset/tourism.owl";
			Model model = ModelFactory.createDefaultModel();

			InputStream in = inputFile;
			if (in == null) {
				throw new IllegalArgumentException("File:" + inputFileName
						+ "not found");
			}
			// model.read(in, " ");
			model.read(in, null);
			
			Query query = QueryFactory.create(queryQ);

			QueryExecution qe = QueryExecutionFactory.create(query, model);

			ResultSet results = qe.execSelect();

			int countResult = 0;

			System.out
					.println("Result\n----------------------------------------");
			dataForTHead = new ArrayList<String>();
			System.out.print(arrSearchProperty.size());
			for (int i = 0; i < arrSearchProperty.size(); i++) {
				String label = arrSearchProperty.get(i).getLabel();
				String language = arrSearchProperty.get(i).getLanguage();
				Boolean select = arrSearchProperty.get(i).isSelect();
				System.out.print("removeIndex.indexOf(i)"+removeIndex.indexOf(i));
				if (select && removeIndex.indexOf(i) >= 0) {
					String temp = label + " " + language + " : ";
					dataForTHead.add(temp);
					System.out.print(temp);
				}
			}
			System.out.println("");
			dataForTBody = new ArrayList<String[]>();
			while (results.hasNext()) {
				QuerySolution row = results.next();
				String[] temp = new String[dataForTHead.size()];
				int j=0;
				for (int i = 0; i < arrSearchProperty.size(); i++) {
					if (removeIndex.indexOf(i) >= 0) {
						continue;
					}
					boolean select = arrSearchProperty.get(i).isSelect();
					String label = arrSearchProperty.get(i).getLabel();
					if (select) {
						temp[j] = row.get(label).toString();
						System.out.print(row.get(label) + " : ");
						System.out.print(temp[j] + " : ");
						j++;
					}
				}
				dataForTBody.add(temp);
				System.out.println();
				countResult++;
			}
			//System.out.println(dataForTBody.get(0)[0]);
			System.out.println("countResult = " + countResult);

			model.close();
			qe.close();
			in.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		String jsonResult = changeResultSetToJSON(dataForTHead, dataForTBody);
		return jsonResult;
	}
	
	private String changeResultSetToJSON(ArrayList<String> dataForTHead,ArrayList<String[]> dataForTBody) {
		System.out.println("changeResultSetToJSON");
		Gson gson = new Gson();
		// dataForTHead to array
		String[] tHead = new String[dataForTHead.size()];
		for(int i=0; i<tHead.length; i++) {
			tHead[i]=dataForTHead.get(i);
		}
		// check
		for(Object temp : tHead) {
			System.out.println("temp:"+temp.toString());
		}
		
		// dataForTBody to array
		String[][] tBody = new String[dataForTBody.size()][];
		for(int i=0; i<tBody.length; i++) {
			tBody[i]=dataForTBody.get(i);
		}
		// check
		//for(String[] temp : tBody) {
		//	for(String temp2 : temp) {
		//		System.out.println("temp2:"+temp2.toString());
		//	}
		//}
		
		//System.out.println("json thead:"+gson.toJson(tHead));
		//System.out.println("json tBody:"+gson.toJson(tBody));
		
		String output = "["+gson.toJson(tHead)+","+gson.toJson(tBody)+"]";
		//System.out.println(output);
		return output;
	}
}

class SearchProperty {
	private boolean select;
	private boolean display;
	private String label;
	private String nameInDB;
	private String keyword;
	private String language;

	public SearchProperty(String label, String nameInDB) {
		setSelect(false);
		setLabel(label);
		setNameInDB(nameInDB);
	}

	public SearchProperty() {
	}

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getNameInDB() {
		return nameInDB;
	}

	public void setNameInDB(String nameInDB) {
		this.nameInDB = nameInDB;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String toString() {
		String result = "";
		result += "select:"+select+"\n";
		result += "display:"+display+"\n";
		result += "label:"+label+"\n";
		result += "nameInDB:"+nameInDB+"\n";
		result += "keyword:"+keyword+"\n";
		result += "language:"+language+"\n";
		return result;
	}
}
