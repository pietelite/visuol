package com.example.visuol;

/* Commented because import library not recognized
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
*/
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
    private int a;
    private int x0;
    private int xp;
    private int b;
    private int y0;
    private int yp;
    private int c;
    private int z0;
    private int zp;
    private int d;
    private boolean xTermPositive;
    private boolean yTermPositive;

    private String objectName;

    /* Commented because program was not running
    private INDArray calXCor() {
        return Nd4j.linspace(xMin,xMax,resolution);
    }
    private INDArray calYCor() {
        return Nd4j.linspace(yMin,yMax,resolution);
    }
    */
    public void setXTermPositive(boolean pos) {
       xTermPositive = pos;
    }
    public void setYTermPositive(boolean pos) {
        yTermPositive = pos;
    }
    private class Vertex {
        private double posX;
        private double posY;
        private double posZ;
        private double textureX;
        private double textureY;
        private int posLineNumber = 0;
        private boolean hasNormal;
    }
    private class Face {
        Vertex[] vertices = new Vertex[4];
    }
    private double calculateZ(double x,double y) {
        if (!xTermPositive) {
            a = -a;
        }
        if (!yTermPositive) {
            b = -b;
        }
        double first = a * Math.pow((x - x0), xp);
        double second = b * Math.pow((y - y0), yp);
        double third = (d - first - second) / c;
        return Math.pow(third, 1/zp) + z0;
    }
    private void generateVectors() {
        /* Commented because program was not running
        double[] xCors = calXCor().toDoubleVector();
        double[] yCors = calYCor().toDoubleVector();
        Vertex[][] vertexMatrix = new Vertex[xCors.length][yCors.length];
        List faces = new ArrayList();
        int a = 0;
        int b = 0;
        for (double xCor : xCors) {
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
        for (int i = 0; i < vertexMatrix.length - 1; i++) {
            for (int j = 0; j < vertexMatrix[0].length; j++) {
                Vertex v = vertexMatrix[i][j];
                if (v.hasNormal) {
                    Vertex vPreviousX = vertexMatrix[i - 1][j];
                    Vertex vFollowingX = vertexMatrix[i + 1][j];
                    Vertex vPreviousY = vertexMatrix[i][j - 1];
                    Vertex vFollowingY = vertexMatrix[i][j + 1];

                }
                Face face = new Face();
                face.vertices[0] = vertexMatrix[i + 0][j + 0];
                face.vertices[1] = vertexMatrix[i + 1][j + 0];
                face.vertices[2] = vertexMatrix[i + 1][j + 1];
                face.vertices[3] = vertexMatrix[i + 0][j + 1];
                faces.add(face);
            }
        }
        */
    }
}
