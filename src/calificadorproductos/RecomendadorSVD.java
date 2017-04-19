/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calificadorproductos;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import java.io.FileNotFoundException;

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
        
        GestorMatriz gm = new GestorMatriz();
        //Crea la matriz, reemplaza ceros por promedios y normaliza.
        Matrix RNorm = gm.crearMatrizRNorm();
        //Descompocicion SVD de la matriz normalizada 
        SingularValueDecomposition svd = gm.computarSVD(RNorm);
        //Prediccion individual pasamos el indice del cliente(fila), el indice producto(columna)
        //la matriz normalizada, la descomposicion SVD y la dimension k a la cual se reducira S U V
        double prediccionIndividual = gm.prediccionIndividual(0, 0, RNorm, svd, 4);
    }

}
