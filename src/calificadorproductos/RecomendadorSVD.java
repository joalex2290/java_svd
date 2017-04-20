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

        System.out.print("Ingrese indice cliente (0-7): ");
        int cliente = lector.nextInt();
        System.out.print("Ingrese indice producto (0-6): ");
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
        
        System.out.println("Prediccion: "+prediccionIndividual);
        
        //CODIGO PARA LLAMAR METODOS USADOS EN LA SEGUNDA PARTE MINIPROYECTO
    }

}
