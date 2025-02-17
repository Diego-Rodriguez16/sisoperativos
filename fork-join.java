import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Main {

    public static void main(String[] args) {
        // Arreglo de ejemplo (puede ser de cualquier clase que implemente Comparable)
        Integer[] datosOriginal = {7, 12, 19, 3, 18, 4, 2, 6, 15, 8};

        System.out.println("Arreglo original: " + Arrays.toString(datosOriginal));

        // Versión con Fork/Join Quicksort
        Integer[] arregloQuick = datosOriginal.clone();
        ForkJoinPool poolQuick = new ForkJoinPool();
        poolQuick.invoke(new ForkJoinQuickSort<>(arregloQuick, 0, arregloQuick.length - 1));
        System.out.println("Arreglo ordenado con ForkJoin QuickSort: " + Arrays.toString(arregloQuick));

        // Versión con Fork/Join Mergesort
        Integer[] arregloMerge = datosOriginal.clone();
        ForkJoinPool poolMerge = new ForkJoinPool();
        poolMerge.invoke(new ForkJoinMergeSort<>(arregloMerge, 0, arregloMerge.length - 1));
        System.out.println("Arreglo ordenado con ForkJoin MergeSort: " + Arrays.toString(arregloMerge));
    }
}

/**
 * Implementación Fork/Join de Quicksort.
 */
class ForkJoinQuickSort<T extends Comparable<? super T>> extends RecursiveAction {

    private static final int UMBRAL = 100; // si el segmento es pequeño, se aplica Insertion Sort
    private final T[] arreglo;
    private final int inicio, fin;

    public ForkJoinQuickSort(T[] arreglo, int inicio, int fin) {
        this.arreglo = arreglo;
        this.inicio = inicio;
        this.fin = fin;
    }

    @Override
    protected void compute() {
        if (inicio < fin) {
            if (fin - inicio < UMBRAL) {
                insertionSort(arreglo, inicio, fin);
            } else {
                int pivoteIndex = partition(arreglo, inicio, fin);
                // Crea dos tareas para ordenar las particiones izquierda y derecha
                ForkJoinQuickSort<T> izquierda = new ForkJoinQuickSort<>(arreglo, inicio, pivoteIndex - 1);
                ForkJoinQuickSort<T> derecha = new ForkJoinQuickSort<>(arreglo, pivoteIndex + 1, fin);
                invokeAll(izquierda, derecha);
            }
        }
    }

    // Método de partición (usando el último elemento como pivote)
    private int partition(T[] arreglo, int inicio, int fin) {
        T pivote = arreglo[fin];
        int i = inicio - 1;
        for (int j = inicio; j < fin; j++) {
            if (arreglo[j].compareTo(pivote) <= 0) {
                i++;
                swap(arreglo, i, j);
            }
        }
        swap(arreglo, i + 1, fin);
        return i + 1;
    }

    private void swap(T[] arreglo, int i, int j) {
        T temp = arreglo[i];
        arreglo[i] = arreglo[j];
        arreglo[j] = temp;
    }

    // Ordenamiento por inserción para segmentos pequeños
    private void insertionSort(T[] arreglo, int inicio, int fin) {
        for (int i = inicio + 1; i <= fin; i++) {
            T clave = arreglo[i];
            int j = i - 1;
            while (j >= inicio && arreglo[j].compareTo(clave) > 0) {
                arreglo[j + 1] = arreglo[j];
                j--;
            }
            arreglo[j + 1] = clave;
        }
    }
}

/**
 * Implementación Fork/Join de Mergesort.
 */
class ForkJoinMergeSort<T extends Comparable<? super T>> extends RecursiveAction {

    private static final int UMBRAL = 100; // si el segmento es pequeño, se aplica Insertion Sort
    private final T[] arreglo;
    private final int inicio, fin;

    public ForkJoinMergeSort(T[] arreglo, int inicio, int fin) {
        this.arreglo = arreglo;
        this.inicio = inicio;
        this.fin = fin;
    }

    @Override
    protected void compute() {
        if (inicio < fin) {
            if (fin - inicio < UMBRAL) {
                insertionSort(arreglo, inicio, fin);
            } else {
                int mitad = (inicio + fin) / 2;
                ForkJoinMergeSort<T> izquierda = new ForkJoinMergeSort<>(arreglo, inicio, mitad);
                ForkJoinMergeSort<T> derecha = new ForkJoinMergeSort<>(arreglo, mitad + 1, fin);
                invokeAll(izquierda, derecha);
                merge(arreglo, inicio, mitad, fin);
            }
        }
    }

    // Fusiona dos subarreglos ordenados: arreglo[inicio..mitad] y arreglo[mitad+1..fin]
    private void merge(T[] arreglo, int inicio, int mitad, int fin) {
        int n1 = mitad - inicio + 1;
        int n2 = fin - mitad;
        @SuppressWarnings("unchecked")
        T[] izquierda = (T[]) new Comparable[n1];
        @SuppressWarnings("unchecked")
        T[] derecha = (T[]) new Comparable[n2];

        for (int i = 0; i < n1; i++) {
            izquierda[i] = arreglo[inicio + i];
        }
        for (int j = 0; j < n2; j++) {
            derecha[j] = arreglo[mitad + 1 + j];
        }

        int i = 0, j = 0, k = inicio;
        while (i < n1 && j < n2) {
            if (izquierda[i].compareTo(derecha[j]) <= 0) {
                arreglo[k++] = izquierda[i++];
            } else {
                arreglo[k++] = derecha[j++];
            }
        }
        while (i < n1) {
            arreglo[k++] = izquierda[i++];
        }
        while (j < n2) {
            arreglo[k++] = derecha[j++];
        }
    }

    // Ordenamiento por inserción para segmentos pequeños
    private void insertionSort(T[] arreglo, int inicio, int fin) {
        for (int i = inicio + 1; i <= fin; i++) {
            T clave = arreglo[i];
            int j = i - 1;
            while (j >= inicio && arreglo[j].compareTo(clave) > 0) {
                arreglo[j + 1] = arreglo[j];
                j--;
            }
            arreglo[j + 1] = clave;
        }
    }
}
