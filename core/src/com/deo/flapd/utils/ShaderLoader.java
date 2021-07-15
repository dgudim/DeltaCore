package com.deo.flapd.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import static com.deo.flapd.utils.DUtils.log;
import static com.deo.flapd.utils.LogLevel.ERROR;
import static com.deo.flapd.utils.LogLevel.INFO;

public final class ShaderLoader {
	public static String BasePath = "";
	public static boolean Pedantic = true;

	public static ShaderProgram fromFile( String vertexFileName, String fragmentFileName ) {
		return ShaderLoader.fromFile( vertexFileName, fragmentFileName, "" );
	}

	public static ShaderProgram fromFile( String vertexFileName, String fragmentFileName, String defines ) {
		String log = "\"" + vertexFileName + "/" + fragmentFileName + "\"";
		if( defines.length() > 0 ) {
			log += " w/ (" + defines.replace( "\n", ", " ) + ")";
		}
		log += "...";
		log("[ShaderLoader]" + " Compiling " + log, INFO);

		String vpSrc = Gdx.files.internal( BasePath + vertexFileName + ".vertex" ).readString();
		String fpSrc = Gdx.files.internal( BasePath + fragmentFileName + ".fragment" ).readString();
		
		return ShaderLoader.fromString( vpSrc, fpSrc, vertexFileName, fragmentFileName, defines );
	}

	public static ShaderProgram fromString( String vertex, String fragment, String vertexName, String fragmentName ) {
		return ShaderLoader.fromString( vertex, fragment, vertexName, fragmentName, "" );
	}

	public static ShaderProgram fromString( String vertex, String fragment, String vertexName, String fragmentName, String defines ) {
		ShaderProgram.pedantic = ShaderLoader.Pedantic;
		ShaderProgram shader = new ShaderProgram( defines + "\n" + vertex, defines + "\n" + fragment );

		if( !shader.isCompiled() ) {
			log( "Shader error:" + shader.getLog(), ERROR);
			Gdx.app.exit();
		}

		return shader;
	}

	private ShaderLoader() {
	}
}
