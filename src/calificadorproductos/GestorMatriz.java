/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calificadorproductos;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author joalexmunoz
 */
public class GestorMatriz {

    private final String archivo = "sparse_matrix.txt";
    private Matrix matrizR;
    private Matrix matrizRBin;
    private Matrix matrizRNorm;
    private final int filas = 7;
    private final int columnas = 6;

    public Matrix crearMatrizRNorm() throws FileNotFoundException {
        //Leer matriz del archivo
        double[][] arrayMatrizR = new double[filas][columnas];
        Scanner lector = new Scanner(new File(this.archivo));

        lector = new Scanner(new File(this.archivo));
        for (int i = 0; i < filas; ++i) {
            for (int j = 0; j < columnas; ++j) {
                if (lector.hasNextDouble()) {
                    arrayMatrizR[i][j] = lector.nextDouble();
                }
            }
        }
        this.matrizR = new Matrix(arrayMatrizR);
        this.matrizRBin = this.crearMatrizBinaria(matrizR);
        System.out.println("R:");
        this.matrizR.print(6, 3);
        //Normalizamos la matriz
        this.matrizRNorm = this.normalizarMatrizR(arrayMatrizR);
        return matrizRNorm;
    }

    public Matrix normalizarMatrizR(double[][] matriz) {
        //Obtiene promedios por columna
        double[] promedioColumnas = new double[columnas];
        for (int i = 0; i < columnas; i++) {
            promedioColumnas[i] = 0;
            for (int j = 0; j < filas; j++) {
                promedioColumnas[i] = promedioColumnas[i] + matriz[j][i];
            }
            promedioColumnas[i] = promedioColumnas[i] / columnas;
        }
        //Remplaza ceros por el promedio de cada columna(producto)
        for (int i = 0; i < columnas; i++) {
            for (int j = 0; j < filas; j++) {
                if (matriz[j][i] == 0) {
                    matriz[j][i] = promedioColumnas[i];
                }
            }
        }
        //Obtiene promedios por fila(cliente)
        double[] promedioFilas = new double[filas];
        for (int i = 0; i < filas; i++) {
            promedioFilas[i] = 0;
            for (int j = 0; j < columnas; j++) {
                promedioFilas[i] = promedioFilas[i] + matriz[i][j];
            }
            promedioFilas[i] = promedioFilas[i] / filas;
        }
        //Resta promedios de cliente a cada fila(cliente)
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                matriz[i][j] = matriz[i][j] - promedioFilas[i];
            }
        }

        Matrix RNorm = new Matrix(matriz);
        System.out.println("RNorm:");
        RNorm.print(6, 3);
        return RNorm;
    }

    public SingularValueDecomposition computarSVD(Matrix matrix) {
        SingularValueDecomposition svd = matrix.svd();
        System.out.println("U:");
        svd.getU().print(6, 3);
        System.out.println("S:");
        svd.getS().print(6, 3);
        System.out.println("V");
        svd.getV().transpose().print(6, 3);
        return svd;
    }

    public double prediccionIndividual(int cliente, int producto, Matrix RNorm, SingularValueDecomposition svd, int k) {
        int m = RNorm.getRowDimension();
        int n = RNorm.getColumnDimension();
        //Obtener Uk
        Matrix Uk = subconjuntoMatriz(svd.getU(), m, k);
        System.out.println(" Uk (k = " + k + ") matrix:");
        Uk.print(6, 3);
        //Obtener Sk
        Matrix Sk = subconjuntoMatriz(svd.getS(), k, k);
        System.out.println(" Sk (k = " + k + " matrix:");
        Sk.print(6, 3);
        //Obtener Vk
        Matrix Vk = subconjuntoMatriz(svd.getV().transpose(), k, n);
        System.out.println(" Vtk (k = " + k + " matrix:");
        Vk.print(6, 3);
        //Método simplificado de Newton para obtener Sk^1/2
        Matrix X = Matrix.identity(k, k);
        for (int i = 0; i < 10; i++) {
            //System.out.println("k = " + i);
            //X.print(6, 3);
            X = X.plus(Sk.times(X.inverse()));
            X = X.times(0.5);
        }
        System.out.println("Sk^1/2");
        X.print(6, 3);
        //Matriz Uk*Sk^1/2
        Matrix A = Uk.times(X);
        System.out.println("Uk*Sk^1/2");
        A.print(6, 3);
        //Matriz Sk^1/2*Vk
        Matrix B = X.times(Vk);
        System.out.println("Vtk*Sk^1/2");
        B.print(6, 3);

        //Codigo para calcular la prediccion, ultima funcion del informe
        // To compute the prediction we simply calculate the dot product of the cth row of 
        //UkSk1/2 and the pth column of Sk^1/2Vk and add the customer average back.
        double prediccion = 0;

        //Obtenemos la fila C de la matriz original
        Matrix tempA = arregloFila(A, cliente, A.getColumnDimension());
        tempA.print(6, 3);
        //Obtenemos la columna P de la matriz original
        Matrix tempB = arregloColumna(B, B.getRowDimension(), producto);
        tempB.print(6, 3);
        //Producto punto entre ellas
        Matrix prod = tempA.times(tempB);

        prediccion = promediarFilas(RNorm, cliente) + prod.get(0, 0);

        return prediccion;
    }

    public void recomendar5Productos(double[][] clienteProductos, Matrix RBin, SingularValueDecomposition svd, int k) {
        int m = RBin.getRowDimension();
        int n = RBin.getColumnDimension();
        //Obtener Uk
        Matrix Uk = subconjuntoMatriz(svd.getU(), m, k);
        System.out.println(" Uk (k = " + k + ") matrix:");
        Uk.print(6, 3);
        //Obtener Sk
        Matrix Sk = subconjuntoMatriz(svd.getS(), k, k);
        System.out.println(" Sk (k = " + k + " matrix:");
        Sk.print(6, 3);
        //Obtener Vk
        Matrix Vk = subconjuntoMatriz(svd.getV().transpose(), k, n);
        System.out.println(" Vtk (k = " + k + " matrix:");
        Vk.print(6, 3);
        //Método simplificado de Newton para obtener Sk^1/2
        Matrix X = Matrix.identity(k, k);
        for (int i = 0; i < 10; i++) {
            //System.out.println("k = " + i);
            //X.print(6, 3);
            X = X.plus(Sk.times(X.inverse()));
            X = X.times(0.5);
        }
        System.out.println("Sk^1/2");
        X.print(6, 3);
        //Matriz Uk*Sk^1/2
        Matrix A = Uk.times(X);
        System.out.println("Uk*Sk^1/2");
        A.print(6, 3);

        Matrix matrizCliente = new Matrix(clienteProductos);
        System.out.println("Cliente");
        matrizCliente.print(3, 2);

        Matrix clientePuntaje = matrizCliente.times(Uk).times(Sk.inverse());
        System.out.println("Cliente puntaje");
        clientePuntaje.print(3, 2);

        System.out.println("Producto punto entre cliente nuevo y los de la matriz original");
        clientePuntaje.times(Vk).print(3, 1);

    }

    //Obtiene un subconjunto de la matriz (reduce a dimension k) necesaria para obtener Sk, Uk, Vk
    private Matrix subconjuntoMatriz(Matrix matriz, int m, int n) {
        Matrix nuevaMatrix = new Matrix(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                nuevaMatrix.set(i, j, matriz.get(i, j));
            }
        }
        return nuevaMatrix;
    }

    //Obtiene el arreglo de la fila c
    private Matrix arregloFila(Matrix matriz, int c, int n) {
        Matrix nuevaMatrix = new Matrix(1, n);
        for (int j = 0; j < n; j++) {
            nuevaMatrix.set(0, j, matriz.get(c, j));
        }
        return nuevaMatrix;
    }

    //Obtiene el arreglo de la columna P 
    private Matrix arregloColumna(Matrix matriz, int m, int p) {
        Matrix nuevaMatrix = new Matrix(m, 1);
        for (int j = 0; j < m; j++) {
            nuevaMatrix.set(j, 0, matriz.get(j, p));
        }
        return nuevaMatrix;
    }

    private double promediarFilas(Matrix matriz, int p) {
        double promedioFilas = 0;
        for (int i = 0; i == p; i++) {
            for (int j = 0; j < matriz.getColumnDimension(); j++) {
                promedioFilas = promedioFilas + matriz.get(i, j);
            }
            promedioFilas = promedioFilas / matriz.getRowDimension();
        }
        return promedioFilas;
    }

    //Obtiene el arreglo de la fila P
    public Matrix crearMatrizBinaria(Matrix matriz) {
        Matrix nuevaMatrix = new Matrix(this.filas, this.columnas);
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (matriz.get(i, j) > 0) {
                    nuevaMatrix.set(i, j, 1);
                }

            }
        }
        return nuevaMatrix;
    }

    public Matrix getMatrizR() {
        return this.matrizR;
    }

    public Matrix getMatrizRBin() {
        return this.matrizRBin;
    }

    public Matrix getMatrizRNorm() {
        return this.matrizRNorm;
    }
}
