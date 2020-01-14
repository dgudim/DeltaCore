package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public abstract class DUtils {
    public static void log(String contents){
        if(Gdx.app.getType() == Application.ApplicationType.Android) {
            FileHandle file = Gdx.files.external("Android/data/!DeltaCore/logFull.txt");
            file.writeString(contents, true);
            FileHandle file2 = Gdx.files.external("Android/data/!DeltaCore/log.txt");
            file2.writeString(contents, true);
        }else {
            FileHandle file = Gdx.files.external("!DeltaCore/logFull.txt");
            file.writeString(contents, true);
            FileHandle file2 = Gdx.files.external("!DeltaCore/log.txt");
            file2.writeString(contents, true);
        }
    }
    public static void clearLog(){
        if(Gdx.app.getType() == Application.ApplicationType.Android) {
            FileHandle file = Gdx.files.external("Android/data/!DeltaCore/log.txt");
            file.writeString("", false);
        }else{
            FileHandle file = Gdx.files.external("!DeltaCore/log.txt");
            file.writeString("", false);
        }
    }
}
