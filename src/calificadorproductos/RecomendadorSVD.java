/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calificadorproductos;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author joalexmunoz
 */
public class RecomendadorSVD {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {

        Scanner lector = new Scanner(System.in);

        System.out.println("---- 1 PARTE PREDICCION CLIENTE ----");
        System.out.print("Ingrese indice matriz para el producto (columna 0-6): ");
        int cliente = lector.nextInt();
        System.out.print("Ingrese indice matriz para el cliente (fila 0-5): ");
        int producto = lector.nextInt();
        lector.close();

        GestorMatriz gm = new GestorMatriz();
        //Crea la matriz, reemplaza ceros por promedios y normaliza.
        Matrix RNorm = gm.crearMatrizRNorm();
        //Descompocicion SVD de la matriz normalizada 
        SingularValueDecomposition svd = gm.computarSVD(RNorm);
        //Prediccion individual pasamos el indice del cliente(fila), el indice producto(columna)
        //la matriz normalizada, la descomposicion SVD y la dimension k a la cual se reducira S U V
        double prediccionIndividual = gm.prediccionIndividual(cliente, producto, RNorm, svd, 4);

        System.out.println("Prediccion: " + prediccionIndividual);
        System.out.println();
        System.out.println("---- 2 PARTE TOP 5 PRODUCTOS ----");
        System.out.println("R Bin");
        gm.getMatrizRBin().print(6, 1);

        SingularValueDecomposition svd2 = gm.computarSVD(gm.getMatrizRBin());

        //productos adquiridos por el nuevo cliente debe m = m de la matriz del proyecto
        double[][] nuevoCliente = {{1, 0, 1, 0, 1, 1, 0}};

        //umbral 0.8, k = 4
        gm.recomendar5Productos(nuevoCliente, 0.8, gm.getMatrizRBin(), svd2, 4);

    }

}
