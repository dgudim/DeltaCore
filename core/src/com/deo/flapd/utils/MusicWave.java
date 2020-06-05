package com.deo.flapd.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import java.io.File;
import java.io.FileInputStream;

public class MusicWave {

    private float[] samples;
    private Music music;

    public MusicWave() {

        String path;

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            path = "Android/data/!DeltaCore/music.mp3";
        } else {
            path = "!DeltaCore/music.mp3";
        }

        File file = Gdx.files.external(path).file();

        try {

            /*
            boolean done = false;

            short[] pcmOut = new short[]{};

            InputStream data = new FileInputStream(file.getPath());
            Bitstream bitStream = new Bitstream(data);
            Decoder decoder = new Decoder();
            while (!done) {
                Header frameHeader = bitStream.readFrame();
                if (frameHeader == null) {
                    done = true;
                } else {
                    //WaveFile waveFile = new WaveFile();
                    //waveFile.Open(path, );
                    //waveFile.Read();
                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitStream);
                    System.out.println("test");
                    short[] next = output.getBuffer();
                    for (int i = 0; i < next.length; i++) System.out.print(" " + next[i]);
                    pcmOut = concatArray(pcmOut, next);
                }
            }
             byte[] bytes = new byte[pcmOut.length];

            for(int i = 0; i<bytes.length; i++){
                bytes[i] = (byte)pcmOut[i];
            }

             */

            FileInputStream f = new FileInputStream(file);
            byte[] bytes = new byte[100000];
            f.read(bytes);

            float[][] twoChannelSamples = getUnscaledAmplitude(bytes, 2);

            float[] averageChannelAmplitude = new float[twoChannelSamples[0].length];
            for (int i = 0; i < averageChannelAmplitude.length; i++) {
                averageChannelAmplitude[i] = (twoChannelSamples[0][i] + twoChannelSamples[1][i]) / 2.0f;
            }

            samples = averageChannelAmplitude;

            music = Gdx.audio.newMusic(Gdx.files.external(path));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSixteenBitSample(int high, int low) {
        return (high << 8) + (low & 0x00ff);
    }

    private float[][] getUnscaledAmplitude(byte[] eightBitByteArray, int nbChannels) {

        float[][] toReturn = new float[nbChannels][eightBitByteArray.length / (2 * nbChannels)];
        int index = 0;

        for (int audioByte = 0; audioByte < eightBitByteArray.length; ) {
            for (int channel = 0; channel < nbChannels; channel++) {
                int low = eightBitByteArray[audioByte];
                audioByte++;
                int high = eightBitByteArray[audioByte];
                audioByte++;
                int sample = getSixteenBitSample(high, low);

                toReturn[channel][index] = sample;
            }
            index++;
        }

        return toReturn;
    }

    public float[] getSamples() {
        return samples;
    }

    public Music getMusic() {
        return music;
    }

}
