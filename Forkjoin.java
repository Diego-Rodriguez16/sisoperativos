import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class Forkjoin {

    // Umbral para decidir si se usa InsertionSort directamente
    private static final int UMBRAL = 100;

    public static void main(String[] args) {
        Integer[] datosOriginal = generarArregloAleatorio(150);

        System.out.println("Arreglo original: " + Arrays.toString(datosOriginal));

        // Si el arreglo es pequeño se ordena directamente con InsertionSort
        if (datosOriginal.length <= UMBRAL) {
            Integer[] arregloInsertion = datosOriginal.clone();
            long inicio = System.nanoTime();
            insertionSort(arregloInsertion, 0, arregloInsertion.length - 1);
            long fin = System.nanoTime();
            System.out.println("\nArreglo ordenado con InsertionSort (arreglo pequeño):");
            System.out.println(Arrays.toString(arregloInsertion));
            System.out.printf("Tiempo de ejecución: %.4f ms%n", (fin - inicio) / 1_000_000.0);
            System.out.println("Solución: InsertionSort");
        } else {
            // Arreglo grande: se prueban las dos versiones Fork/Join

            // Versión Fork/Join QuickSort
            Integer[] arregloQuick = datosOriginal.clone();
            ForkJoinPool poolQuick = new ForkJoinPool();
            long inicioQuick = System.nanoTime();
            poolQuick.invoke(new ForkJoinQuickSort<>(arregloQuick, 0, arregloQuick.length - 1, UMBRAL));
            long finQuick = System.nanoTime();
            System.out.println("\nArreglo ordenado con ForkJoin QuickSort (usa InsertionSort para segmentos pequeños):");
            System.out.println(Arrays.toString(arregloQuick));
            System.out.printf("Tiempo de ejecución QuickSort: %.4f ms%n", (finQuick - inicioQuick) / 1_000_000.0);
            System.out.println("Solución: ForkJoin QuickSort (con InsertionSort en segmentos pequeños)");

            // Versión Fork/Join MergeSort
            Integer[] arregloMerge = datosOriginal.clone();
            ForkJoinPool poolMerge = new ForkJoinPool();
            long inicioMerge = System.nanoTime();
            poolMerge.invoke(new ForkJoinMergeSort<>(arregloMerge, 0, arregloMerge.length - 1, UMBRAL));
            long finMerge = System.nanoTime();
            System.out.println("\nArreglo ordenado con ForkJoin MergeSort (usa InsertionSort para segmentos pequeños):");
            System.out.println(Arrays.toString(arregloMerge));
            System.out.printf("Tiempo de ejecución MergeSort: %.4f ms%n", (finMerge - inicioMerge) / 1_000_000.0);
            System.out.println("Solución: ForkJoin MergeSort (con InsertionSort en segmentos pequeños)");
        }
    }

    private static Integer[] generarArregloAleatorio(int tamaño) {
        Integer[] arreglo = new Integer[tamaño];
        for (int i = 0; i < tamaño; i++) {
            arreglo[i] = (int) (Math.random() * 1000);
        }
        return arreglo;
    }


    // Ordenamiento por inserción (Insertion Sort) aplicado a un segmento del arreglo.
    private static <T extends Comparable<? super T>> void insertionSort(T[] arreglo, int inicio, int fin) {
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
 * Implementación Fork/Join de QuickSort.
 * Si el segmento es menor o igual al umbral se usa InsertionSort.
 */
class ForkJoinQuickSort<T extends Comparable<? super T>> extends RecursiveAction {

    private final T[] arreglo;
    private final int inicio, fin, umbral;

    public ForkJoinQuickSort(T[] arreglo, int inicio, int fin, int umbral) {
        this.arreglo = arreglo;
        this.inicio = inicio;
        this.fin = fin;
        this.umbral = umbral;
    }

    @Override
    protected void compute() {
        if (inicio < fin) {
            if (fin - inicio + 1 <= umbral) {
                // Caso base: segmento pequeño
                insertionSort(arreglo, inicio, fin);
            } else {
                int pivoteIndex = partition(arreglo, inicio, fin);
                ForkJoinQuickSort<T> izquierda = new ForkJoinQuickSort<>(arreglo, inicio, pivoteIndex - 1, umbral);
                ForkJoinQuickSort<T> derecha = new ForkJoinQuickSort<>(arreglo, pivoteIndex + 1, fin, umbral);
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
 * Implementación Fork/Join de MergeSort.
 * Si el segmento es menor o igual al umbral se usa InsertionSort.
 */
class ForkJoinMergeSort<T extends Comparable<? super T>> extends RecursiveAction {

    private final T[] arreglo;
    private final int inicio, fin, umbral;

    public ForkJoinMergeSort(T[] arreglo, int inicio, int fin, int umbral) {
        this.arreglo = arreglo;
        this.inicio = inicio;
        this.fin = fin;
        this.umbral = umbral;
    }

    @Override
    protected void compute() {
        if (inicio < fin) {
            if (fin - inicio + 1 <= umbral) {
                insertionSort(arreglo, inicio, fin);
            } else {
                int mitad = (inicio + fin) / 2;
                ForkJoinMergeSort<T> izquierda = new ForkJoinMergeSort<>(arreglo, inicio, mitad, umbral);
                ForkJoinMergeSort<T> derecha = new ForkJoinMergeSort<>(arreglo, mitad + 1, fin, umbral);
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
