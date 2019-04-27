package com.example.visuol;

/*
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
*/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class CreateObject {
    /** The maximum of x coordinates.*/
    private double xMax = 0.8;
    /** The minimum of x coordinates.*/
    private double xMin = -0.8;
    /** The maximum of y coordinates.*/
    private double yMax = 0.8;
    /** The minimum of y coordinates.*/
    private double yMin = -0.8;
    /** The points being taken.*/
    private long resolution = 100;

    /** Equation: a*(x-xo)**xP + b*(y-yo)**yP + c*(z-zo)^zP = d */
    private static int a = 2;
    private static int x0 = 1;
    private static int xp = 2;
    private static int b = 1;
    private static int y0 = 1;
    private static int yp = 2;
    private static int c = 1;
    private static int z0 = 2;
    private static int zp = 1;
    private static int d = 1;
    private static boolean xTermPositive;
    private static boolean yTermPositive;

    /*
    private double[] defaultNormal = {0.0,0.0,1.0};
    private String objectName;
    private static Vertex[][] vertexs;
    private static List<Face> facess;

    public static Vertex[][] getVertexs() {
        return vertexs;
    }
    public static List getFacess() {
        return facess;
    }

    private INDArray calXCor() {
        return Nd4j.linspace(xMin,xMax,resolution);
    }
    private INDArray calYCor() {
        return Nd4j.linspace(yMin,yMax,resolution);
    }
    private void setXTermPositive(boolean pos) {
       xTermPositive = pos;
    }
    private void setYTermPositive(boolean pos) {
        yTermPositive = pos;
    }
    private class Vertex {
        private double posX;
        private double posY;
        private double posZ;
        private double[] normal;
        private double textureX;
        private double textureY;
        private int posLineNumber;
        private int texLineNumber;
        private int norLineNumber;
        private boolean hasNormal;
    }
    private class Face {
        Vertex[] vertices = new Vertex[4];
    }
    private static double calculateZ(double x,double y) {
        if (!xTermPositive) {
            a = -a;
        }
        if (!yTermPositive) {
            b = -b;
        }
        double first = a * Math.pow((x - x0), xp);
        double second = b * Math.pow((y - y0), yp);
        double third = (d - first - second) / c;
        System.out.println(Math.pow(third, 1/zp) + z0);
        return Math.pow(third, 1/zp) + z0;
    }
    private void generateVectors() {
        double[] xCors = calXCor().toDoubleVector();
        double[] yCors = calYCor().toDoubleVector();
        Vertex[][] vertexMatrix = new Vertex[xCors.length][yCors.length];
        List<Face> faces = new ArrayList<>();
        int a = 0;
        for (double xCor : xCors) {
            int b = 0;
            for (double yCor : yCors) {
                Vertex v = new Vertex();
                v.posX = xCor;
                v.posY = yCor;
                v.posZ = calculateZ(xCor,yCor);
                v.textureX = (xCor - xMin) / (xMax - xMin);
                v.textureY = (yCor - yMin) / (yMax - yMin);
                if (xCor == xMin || xCor == xMax || yCor == yMin || yCor == yMax) {
                    v.hasNormal = false;
                } else {
                    v.hasNormal = true;
                }
                vertexMatrix[a][b] = v;
                b++;
            }
            a++;
        }
        for (int i = 1; i < vertexMatrix.length - 1; i++) {
            for (int j = 1; j < vertexMatrix[0].length - 1; j++) {
                Vertex v = vertexMatrix[i][j];
                if (v.hasNormal) {
                    Vertex vPreviousX = vertexMatrix[i - 1][j];
                    Vertex vFollowingX = vertexMatrix[i + 1][j];
                    Vertex vPreviousY = vertexMatrix[i][j - 1];
                    Vertex vFollowingY = vertexMatrix[i][j + 1];
                    double[] xVec = {vFollowingX.posX - vPreviousX.posX, vFollowingX.posY - vPreviousX.posY, vFollowingX.posZ - vPreviousX.posZ};
                    double[] yVec = {vFollowingY.posX - vPreviousY.posX, vFollowingY.posY - vPreviousY.posY, vFollowingY.posZ - vPreviousY.posZ};
                    double[] normalA = crossProduct(xVec, yVec);
                    v.normal = normalA;
                }
                Face face = new Face();
                face.vertices[0] = vertexMatrix[i + 0][j + 0];
                face.vertices[1] = vertexMatrix[i + 1][j + 0];
                face.vertices[2] = vertexMatrix[i + 1][j + 1];
                face.vertices[3] = vertexMatrix[i + 0][j + 1];
                faces.add(face);
            }
        }
        vertexs = vertexMatrix;
        facess = faces;
    }
    private double[] crossProduct(double[] xVec, double[] yVec) {
        double[] cross = new double[3];
        cross[0] = xVec[1] * yVec[2] - xVec[2] * yVec[1];
        cross[1] = xVec[2] * yVec[0] - xVec[0] * yVec[2];
        cross[2] = xVec[0] * yVec[1] - xVec[1] * yVec[0];
        return cross;
    }

    private int countPos = 0;
    private int countTexture = 0;
    private int countNormal = 0;
    private static final String filename = "src/newObject.txt";
    public URL getPackageLocation() {
        return getClass().getResource(".");
    }
    public void writeObject() {
        try {
            generateVectors();
            DecimalFormat df = new DecimalFormat("%.8f");
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("g defalut\n");
            writer.write("#Vertices:\n");
            int posLineCount = 0;
            int texLineCount = 0;
            int norLineCount = 0;
            for (int i = 0; i < vertexs.length; i++) {
                for (int j = 0; j < vertexs[0].length; j++) {
                    writer.write("\n");
                    posLineCount ++;
                    Vertex v = vertexs[i][j];
                    v.posLineNumber = posLineCount;
                    writer.write("v " + df.format(v.posX) + " " + df.format(v.posZ) + " " + df.format(v.posY));
                }
            }
            for (int i = 0; i < vertexs.length; i++) {
                for (int j = 0; j < vertexs[0].length; j++) {
                    writer.write("\n");
                    texLineCount ++;
                    Vertex v = vertexs[i][j];
                    v.texLineNumber = texLineCount;
                    writer.write("vt " + df.format(v.textureX) + " " + df.format(v.textureY));
                }
            }
            writer.write("\n");
            writer.write("\n");
            norLineCount++;
            writer.write("vn " + df.format(defaultNormal[0]) + " " +df.format(defaultNormal[1]));
            writer.write(" " + df.format(defaultNormal[2]));
            for (int i = 0; i < vertexs.length; i++) {
                for (int j = 0; j < vertexs[0].length; j++) {
                    Vertex v = vertexs[i][j];
                    if (v.hasNormal) {
                        writer.write("\n");
                        norLineCount ++;
                        v.norLineNumber = norLineCount;
                        writer.write("vt " + df.format(v.normal[0]) + " " + df.format(v.normal[1]) + " " + df.format(v.normal[2]));
                    } else {
                        v.norLineNumber = 1;
                    }
                }
            }
            writer.write("\n");
            writer.write("#Faces");
            writer.write("\n");
            for (Face face : facess) {
                writer.write("\n");
                writer.write("f ");
                for (Vertex v : face.vertices) {
                    writer.write(v.posLineNumber + "/" + v.texLineNumber + "/" + v.norLineNumber);
                }
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
*/
}

