package com.adcash.mobileads;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class Log {
    private static final String LOGTAG = "AdcashLogger";

    // Set to false to remove creation of local file
    private static final boolean IS_DEBUG_MODE = false;

    private static final SimpleDateFormat mSDF = new SimpleDateFormat( "MM-dd hh:mm:ss.SSS" );

    public Log() {

    }

    public static void e( String tag, String msg ) {
    	android.util.Log.e( tag, msg );

        writeToFile( tag, msg );
    }
    
    public static void i( String tag, String msg ) {
    	android.util.Log.i( tag, msg );

        writeToFile( tag, msg );
    }
    
    public static void v( String tag, String msg ) {
    	android.util.Log.v( tag, msg );

        writeToFile( tag, msg );
    }
    
    public static void w( String tag, String msg ) {
    	android.util.Log.w( tag, msg );

        writeToFile( tag, msg );
    }
    
    public static void d( String tag, String msg ) {
    	android.util.Log.d( tag, msg );

        writeToFile( tag, msg );
    }

    public static void d( String tag, String msg, Throwable th ) {
    	android.util.Log.d( tag, msg, th );

        writeToFile( tag, msg, th );
    }

    private static void writeToFile( String tag, String msg ) {

        if( IS_DEBUG_MODE ) {
            File root = Environment.getExternalStorageDirectory();
            File file = new File( root, "appLog.txt" );

            try {
                if( root.canWrite() ) {
                    FileWriter fileWriter = new FileWriter( file, true );
                    BufferedWriter out = new BufferedWriter( fileWriter );

                    Date d = new Date();

                    out.write( mSDF.format( d ) + ": " + tag + " : " + msg );
                    out.newLine();
                    out.close();
                }
            } catch( IOException e ) {
            	android.util.Log.d( LOGTAG, "Couldn't write file: " + e.getMessage() );
            }
        }
    }

    private static void writeToFile( String tag, String msg, Throwable th ) {
        writeToFile( tag, msg + ", e: " + th.getMessage() );
    }

}
