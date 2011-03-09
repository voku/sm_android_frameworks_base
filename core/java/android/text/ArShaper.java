/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.text;

import android.graphics.Paint;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
import java.lang.Character.*;
import java.util.MissingResourceException;

/**
 * This class is used to shape Arabic letters in different formats
 */
public class ArShaper {


	static boolean debugG = false;
	private static boolean webkitAr = false; // for PhoneLayoutPolicy, for use in webkitAr
	static private int AShapBits = ArabicShaping.LETTERS_SHAPE|ArabicShaping.LAMALEF_NEAR;


   //
   // Arabic Shaping: Set webkitAr field.
   // For use by webkitAr implmentation
   //
    public static void webkitSet(boolean bool) {
	webkitAr = bool;	
    }

   //
   // Arabic Shaping: Get webkitAr field.
   // For use by webkitAr implmentation
   //
    public static boolean getWebkit() {
	return webkitAr;	
    }

   
 	//
	//	Arabic Shaper for char[] types
	//
    
    public static void shaper(char[] mText, final int start, final int end,String info) {
    	
    	ArabicShaping AShaping = new ArabicShaping(AShapBits);

    	
    	
    	try	 {
    			AShaping.shape(mText,start,end);
    		}	
    		

    	catch (ArabicShapingException e){
    				System.out.print("Cannot shape at" + info);
    		}


		// Test to remove extra space after lamAlef

		/*for(int j=start ;j<end;j++){

			if (j+1 >= end);

			else if(	(mText[j] >= '\uFEF5' & mText[j] >= '\uFEFC' ) &
					 mText[j+1] == ' ')
				{

					if(j+2 >=end);

					else {
					mText[j+1]=mText[j+2];
					j++;
					}
	
				}

			else mText[j]=mText[j];
		}*/


    }

     /**
		Arabic Shaper for char[] types with no start and end (used in SSB change function)
     */
    public static String shaper(char[] mText,String info) {
    	
    	ArabicShaping AShaping = new ArabicShaping(AShapBits);

    	String shaped = null;
    	StringBuffer st = new StringBuffer();
    	st.append(mText);

    	
    	
    	try	 {
    			shaped = AShaping.shape(st.toString());
    		}	
    		

    	catch (ArabicShapingException e){
    				System.out.print("Cannot shape at " + info);
    		}

	return shaped;

    }
    
    /**
	Arabic Shaper for CharSequence types
     */
    
    public static String shaper(CharSequence mText, String info) {
	
	ArabicShaping AShaping = new ArabicShaping(AShapBits);

	String shaped = null;
	
	
	try	 {
			shaped=AShaping.shape(mText.toString());
		}	
		

	catch (ArabicShapingException e){
				System.out.print("Cannot shape at" + info);
		}
	
	return shaped;

    }

	
     /**
	Arabic Shaper for String types
     */
    
    public static String shaper(String mText, String info) {
	
	ArabicShaping AShaping = new ArabicShaping(AShapBits);

	String shaped = null;

	
	
	try	 {
			shaped=AShaping.shape(mText);
		}	
		

	catch (ArabicShapingException e){
				System.out.print("Cannot shape at " + info);
		}
	
	return shaped;

    }




    /**
	Arabic Shaper for String types with start and end
     */
    
    public static String shaper(String mText,final int start,final int end, String info) {
	
	ArabicShaping AShaping = new ArabicShaping(AShapBits);


	char[] shaped = mText.toCharArray();
	
	
	try	 {
			AShaping.shape(shaped,0,end-start);
		}	
		

	catch (ArabicShapingException e){
				System.out.print("Cannot shape at " + info);
		}

	StringBuffer st = new StringBuffer();
	st.append(shaped,0,shaped.length);
	
	
	return st.toString();

    }

    /**
     * Arabic Shaping for text going through canvas.
     * Function takes argument char[] and shapes its arabic letters for connectivity
     */

    public static void shapeText(char[] text,int index,int count,Paint paint,String pos){

	if(debugG)
		{System.out.println(pos + "-- Org---Paint Hash :" );
		String prt = String.copyValueOf(text,index,count);
		System.out.println(prt);
		System.out.println(count);

		Thread.dumpStack();
		}
	



	boolean containsArabic = false;
	int textLength = count;

	for(int chk=index;chk<textLength;chk++){
		if ( Character.UnicodeBlock.of(text[chk]) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(text[chk]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(text[chk]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ){

				containsArabic = true;

				break;
			}
			

		}

	if(!containsArabic) {

			if(debugG) System.out.println(pos + "-- not to be reveresed or shaped" );

			return;
		}	

	
	
	String[] doRev = new String[2];
	int reverse = 0;
	
	// tracking android.text.Styled
	doRev[0] = (((Thread.currentThread()).getStackTrace())[5]).getClassName(); 
	doRev[1] = (((Thread.currentThread()).getStackTrace())[6]).getClassName(); 
	//System.out.println("Class is 0 :" + doRev[0] );
	//System.out.println("Class is 1 :" + doRev[1] );

	//Reverse chars code - works for iSilo,Gmail..etc
	// If it goes through android.Text, then there's no need for reversing, else do reversing


	if ( ((!( doRev[0].contains("android.text.Styled") ))  & 
	     (!( doRev[1].contains("android.text.Styled") )) )  ){       
		// check to see if we are using text other than
		// android's type

		reverse = 1;


				
	//Thread.dumpStack();    				  

	char[] revText= new char[textLength];
	int j = 0,pun=0;
	
	StringBuffer sBuilderTmp = new StringBuffer();
	sBuilderTmp.append(text,index,textLength);
	StringBuffer sBuilder = new StringBuffer(reverse == 1 ? (sBuilderTmp.reverse()).toString():sBuilderTmp.toString() );
	sBuilderTmp.delete(0,sBuilderTmp.length());
	
	
	for(int i=index;i<textLength;i++){

		if (debugG) System.out.println("Rev Loop where text is:" + text[i] +" ,at " + i 
			+"and sBuilder is"+ sBuilder.charAt(i) );


		if(	(sBuilder.charAt(i) >= '\u0000' & sBuilder.charAt(i) <= '\u002f' ) ||
			(sBuilder.charAt(i) >= '\u003A' & sBuilder.charAt(i) <= '\u0040' ) ||
			(sBuilder.charAt(i) >= '\u005B' & sBuilder.charAt(i) <= '\u0060' ) ||
			(sBuilder.charAt(i) >= '\u007B' & sBuilder.charAt(i) <= '\u007E' ) )
				{

			if (debugG) System.out.println("pun --> " + sBuilder.charAt(i) + " ," + i );

			switch (sBuilder.charAt(i)){
					
					case '\u0028':
						text[i] = '\u0029';
						break;

					case '\u0029':
						text[i] = '\u0028';
						break;

					case '\u003c':
						text[i] = '\u003e';
						break;

					case '\u003e':
						text[i] = '\u003c';
						break;

					case '\u005b':
						text[i] = '\u005d';
						break;

					case '\u005d':
						text[i] = '\u005b';
						break;

					case '\u007b':
						text[i] = '\u007d';
						break;

					case '\u007d':
						text[i] = '\u007b';
						break;
					default:
						text[i] = sBuilder.charAt(i);						
						break;

				}
			if (debugG) System.out.println("pun done--> " + text[i]  );
			

		}


		else if( !(Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ) ){

			do{
				
				//System.out.println("text is " + text[i]);

				switch (sBuilder.charAt(i)) {

					case '\u0028':
						sBuilderTmp.append('\u0029');
						break;

					case '\u0029':
						sBuilderTmp.append('\u0028');
						break;

					case '\u003c':
						sBuilderTmp.append('\u003e');
						break;

					case '\u003e':
						sBuilderTmp.append('\u003c');
						break;

					case '\u005b':
						sBuilderTmp.append('\u005d');
						break;

					case '\u005d':
						sBuilderTmp.append('\u005b');
						break;

					case '\u007b':
						sBuilderTmp.append('\u007d');
						break;

					case '\u007d':
						sBuilderTmp.append('\u007b');
						break;

					default:
						sBuilderTmp.append(
							sBuilder.charAt(i));						
						break;

				}

				if (debugG) 
				System.out.println("char to be rev is " + sBuilderTmp.charAt(j) + " at " + i);

				i++;
				j++;
				
				if( i >= textLength ){
				
					break;	}			

				}while(!(Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ));

				if (debugG) 
				System.out.println("text to be rev is " + sBuilderTmp.toString() + " at " + i);

			while (true){

			if (i >= textLength){ i--;
						   j--;
						   break;
						}

			else if (	
					i < textLength &&
					
					(Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(sBuilder.charAt(i)) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ))
					{
					
					
					i--; 
					j--;
				
					
				}

			

			else {						
				break;
				}

			}
					
					
			
			sBuilderTmp.trimToSize();
			String tmp = (sBuilderTmp.reverse()).toString();
			tmp.getChars(0,tmp.length(),revText,0);

			//System.out.println("Trans. revText in text at loop " + i);
			int k=i-j;
			j=0;
			for(;k<=i;k++){
					if (debugG) System.out.println("changing char at " + k);
					text[k] = revText[j]; 
					j++;

				}

			j=0;
			
			
			if (debugG) System.out.println("Reversed --> " + sBuilderTmp.toString());
			

			sBuilderTmp.delete(0,sBuilderTmp.length());
		


		}

		else  {text[i] = sBuilder.charAt(i);
			
			if (debugG) System.out.println("This is a Arab char --> "+sBuilder.charAt(i)+" at " +i);

			}

		} // end of first for loop

	



		ArabicShaping AShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE|ArabicShaping.LAMALEF_AUTO
			|ArabicShaping.TEXT_DIRECTION_VISUAL_LTR);

	try {  //Shaping code
		AShaping.shape(text,index,count);
		
	}

	catch (ArabicShapingException e){
				System.out.print("Cannot Convert Canvas Text in " + pos);
		}
	catch (NullPointerException e){
				System.out.print("Null,Cannot Convert Canvas Text in " + pos);
		}

	if(debugG){
		System.out.println("----And after shaping in "+ pos +" :");
		String prt = String.copyValueOf(text,index,count);
		System.out.println(prt);
		System.out.println(textLength);
		}



	}//end of check for android.text


	}

     /**
     * Arabic Shaping for text going through canvas.
     * Function takes argument string and shapes its arabic letters for connectivity
     */

    public static String shapeText(String text,String pos){

	String textR = shapeText(text,0,text.length(),pos);

	//ArabicShaping AShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE|ArabicShaping.LAMALEF_AUTO);
	//Thread.dumpStack();
	/*System.out.println("---");
	System.out.println(text);
	System.out.println(text.length());
	System.out.println("And after shaping in "+ pos +" :");*/
	
	/*try {
		text = AShaping.shape(text);
	}

	catch (ArabicShapingException e){
				System.out.print("Cannot Convert Canvas Text in " + pos);
		}
	catch (NullPointerException e){
				System.out.print("Null,Cannot Convert Canvas Text in " + pos);
		}

	System.out.println(text);
	System.out.println(text.length());

	return text;*/

	return textR;

	}


    /**
     * Arabic Shaping for text going through canvas.
     * Function takes argument string (with start and end) and shapes its arabic letters for connectivity
     */

    public static String shapeText(String text,int start,int end,String pos){

	if(debugG){
	System.out.println(pos + "-- Org---Paint Hash :" );
	System.out.println(text.substring(start,end));
	System.out.println(text.substring(start,end).length());
		}

	char[] shaped = text.toCharArray();

	if (debugG) Thread.dumpStack();




	boolean containsArabic = false;
	int textLength = end-start;

	for(int chk=start;chk<textLength;chk++){
		if ( Character.UnicodeBlock.of(shaped[chk]) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(shaped[chk]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(shaped[chk]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ){
				containsArabic = true;
				break;
			}
			

		}

	if(!containsArabic) {

			if(debugG) System.out.println(pos + "-- not to be reveresed or shaped" );

			return String.copyValueOf(shaped);
		}	



	String[] doRev = new String[2];


	// tracking android.text.Styled
	doRev[0] = (((Thread.currentThread()).getStackTrace())[5]).getClassName(); 
	doRev[1] = (((Thread.currentThread()).getStackTrace())[6]).getClassName();

	//System.out.println("Class is 0 :" + doRev[0] );
	//System.out.println("Class is 1 :" + doRev[1] );

	//System.out.println("Class is :" + doRev );

	//System.out.println("--------------");
	//System.out.println(text);
	//System.out.println(text.length());
	
	
	

	//Reverse chars code - same as char[] reversing


	if ( (!( doRev[0].contains("android.text.Styled") ))  & 
	     (!( doRev[1].contains("android.text.Styled") )) ){  

	// check to see if we are using text other than
	// android's type
	//Thread.dumpStack();

	ArabicShaping AShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE|ArabicShaping.LAMALEF_AUTO
			|ArabicShaping.TEXT_DIRECTION_VISUAL_LTR); // only for drawText string type
								   // as text comes normal and not reversed like
								   // char[] drawText

	char[] revText= new char[shaped.length];
	int j = 0;
	String tmp = null;
	StringBuffer sBuilder = new StringBuffer();
	
	
	for(int i=0;i<shaped.length;i++){

		if(Character.UnicodeBlock.of(shaped[i]) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(shaped[i]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(shaped[i]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B ){

			do{
			
				sBuilder.append(shaped[i]);
				i++;
				j++;
				if( i >= shaped.length )
					break;

				}while(Character.UnicodeBlock.of(shaped[i]) == Character.UnicodeBlock.ARABIC ||
		Character.UnicodeBlock.of(shaped[i]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
		Character.UnicodeBlock.of(shaped[i]) == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B  ||
					
					(shaped[i] >= '\u0000' & shaped[i] <= '\u002F' ) ||
					(shaped[i] >= '\u003A' & shaped[i] <= '\u0040' ) ||
					(shaped[i] >= '\u005B' & shaped[i] <= '\u0060' ) ||
					(shaped[i] >= '\u007B' & shaped[i] <= '\u007E' ));

			


			if (		i < shaped.length &&
					  (shaped[i] == '\u0020') ){
					
					
					i--; // Remove last punctuation if not between arabic characters
					j--;
					sBuilder.deleteCharAt(j);
				}
					
					
			
			sBuilder.trimToSize();
			tmp = (sBuilder.reverse()).toString();
			tmp.getChars(0,tmp.length(),revText,0);

			//System.out.println("Trans. revText in text at loop " + i);
			int k=i-j;
			j=0;
			for(;k<i;k++){
					
					shaped[k] = revText[j]; 
					j++;

				}

			j=0;
			
			//System.out.print("Reversed --> ");
			//System.out.println(tmp);

			sBuilder.delete(0,sBuilder.length());


		}

		//else

			//System.out.println("This is an Arabic char --> "+text[i]);

	}//end of first for loop

	
	try {

		if (debugG) System.out.println("start is " + start +",end is" + end );
		AShaping.shape(shaped,start,end-start);
	}

	catch (ArabicShapingException e){
				System.out.print("Cannot Convert Canvas Text in " + pos);
		}
	catch (NullPointerException e){
				System.out.print("Null,Cannot Convert Canvas Text in " + pos);
		}

		if(debugG)
		{System.out.println("And after shaping in "+ pos +" :");
		System.out.println(String.copyValueOf(shaped,start,end-start));
		System.out.println(shaped.length);}




	} // end of check for android.text
	


	/*else {

	//AShaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE|ArabicShaping.LAMALEF_AUTO);

	ArabicShaping AShaping = new ArabicShaping(ArabicShaping.LETTERS_UNSHAPE|
					ArabicShaping.LAMALEF_AUTO); 

	
	
	
	}*/
	
	

	return String.copyValueOf(shaped);	

    }
    


}


