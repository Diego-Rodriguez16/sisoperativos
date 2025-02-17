#include <iostream>
#include <cstdlib>
#include <pthread.h>
#include <chrono>
#include <cstdio>

#define NUM_HILOS 27

using namespace std;
using namespace std::chrono;

struct Parametros {
    int fila;
    int columna;
    int (*matriz)[9];
};

int validaciones[NUM_HILOS] = {0};

void *verificarSubcuadro(void *param);
void *verificarFila(void *param);
void *verificarColumna(void *param);
int verificarSubcuadros(int sudoku[9][9]);
int verificarLinea(int linea[9]);
int validarSudokuUnHilo(int sudoku[9][9]);

int main() {
    int sudoku[9][9] = {
        {6, 2, 4, 5, 3, 9, 1, 8, 7},
        {5, 1, 9, 7, 2, 8, 6, 3, 4},
        {8, 3, 7, 6, 1, 4, 2, 9, 5},
        {1, 4, 3, 8, 6, 5, 7, 2, 9},
        {9, 5, 8, 2, 4, 7, 3, 6, 1},
        {7, 6, 2, 3, 9, 1, 4, 5, 8},
        {3, 7, 1, 9, 5, 6, 8, 4, 2},
        {4, 9, 6, 1, 8, 2, 5, 7, 3},
        {2, 8, 5, 4, 7, 3, 9, 1, 6}
    };

    // Comprobación usando un único hilo
    auto inicioUnHilo = steady_clock::now();
    if (validarSudokuUnHilo(sudoku))
        cout << "Chequeo con un solo hilo: ¡El sudoku es INVÁLIDO!" << endl;
    else
        cout << "Chequeo con un solo hilo: ¡El sudoku es VÁLIDO!" << endl;
    auto finUnHilo = steady_clock::now();
    duration<double> tiempoUnHilo = duration_cast<duration<double>>(finUnHilo - inicioUnHilo);
    cout << "\nTiempo total usando un único hilo: " << tiempoUnHilo.count() << " segundos\n" << endl;

    // Comprobación usando 27 hilos
    auto inicioMultihilo = steady_clock::now();
    pthread_t hilos[NUM_HILOS];
    int indiceHilo = 0;

    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            // Hilo para verificar cada subcuadro 3x3
            if (i % 3 == 0 && j % 3 == 0) {
                Parametros *datoSub = (Parametros *) malloc(sizeof(Parametros));
                datoSub->fila = i;
                datoSub->columna = j;
                datoSub->matriz = sudoku;
                pthread_create(&hilos[indiceHilo++], NULL, verificarSubcuadro, datoSub);
            }
            // Hilo para cada fila
            if (j == 0) {
                Parametros *datoFila = (Parametros *) malloc(sizeof(Parametros));
                datoFila->fila = i;
                datoFila->columna = j;
                datoFila->matriz = sudoku;
                pthread_create(&hilos[indiceHilo++], NULL, verificarFila, datoFila);
            }
            // Hilo para cada columna
            if (i == 0) {
                Parametros *datoCol = (Parametros *) malloc(sizeof(Parametros));
                datoCol->fila = i;
                datoCol->columna = j;
                datoCol->matriz = sudoku;
                pthread_create(&hilos[indiceHilo++], NULL, verificarColumna, datoCol);
            }
        }
    }

    for (int i = 0; i < NUM_HILOS; i++) {
        pthread_join(hilos[i], NULL);
    }

    bool esValido = true;
    for (int i = 0; i < NUM_HILOS; i++) {
        if (validaciones[i] == 0) {
            esValido = false;
            break;
        }
    }
    if (esValido)
        cout << "Chequeo con múltiples hilos: ¡El sudoku es VÁLIDO!" << endl;
    else
        cout << "Chequeo con múltiples hilos: ¡El sudoku es INVÁLIDO!" << endl;

    auto finMultihilo = steady_clock::now();
    duration<double> tiempoMultihilo = duration_cast<duration<double>>(finMultihilo - inicioMultihilo);
    cout << "\nTiempo total usando 27 hilos: " << tiempoMultihilo.count() << " segundos" << endl;

    return 0;
}

void *verificarSubcuadro(void *param) {
    Parametros *datos = (Parametros *) param;
    int inicioFila = datos->fila;
    int inicioCol = datos->columna;
    int chequeo[10] = {0};

    for (int i = inicioFila; i < inicioFila + 3; ++i) {
        for (int j = inicioCol; j < inicioCol + 3; ++j) {
            int valor = datos->matriz[i][j];
            if (chequeo[valor] != 0) {
                free(datos);
                pthread_exit(NULL);
            }
            chequeo[valor] = 1;
        }
    }
    // Ubicación: 0-8 según el subcuadro (fila/columna)
    int pos = (inicioFila / 3) * 3 + (inicioCol / 3);
    validaciones[pos] = 1;
    free(datos);
    pthread_exit(NULL);
}

void *verificarFila(void *param) {
    Parametros *datos = (Parametros *) param;
    int fila = datos->fila;
    int chequeo[10] = {0};

    for (int j = 0; j < 9; j++) {
        int valor = datos->matriz[fila][j];
        if (chequeo[valor] != 0) {
            free(datos);
            pthread_exit(NULL);
        }
        chequeo[valor] = 1;
    }
    validaciones[9 + fila] = 1;
    free(datos);
    pthread_exit(NULL);
}

void *verificarColumna(void *param) {
    Parametros *datos = (Parametros *) param;
    int columna = datos->columna;
    int chequeo[10] = {0};

    for (int i = 0; i < 9; i++) {
        int valor = datos->matriz[i][columna];
        if (chequeo[valor] != 0) {
            free(datos);
            pthread_exit(NULL);
        }
        chequeo[valor] = 1;
    }
    validaciones[18 + columna] = 1;
    free(datos);
    pthread_exit(NULL);
}

int verificarLinea(int linea[9]) {
    int chequeo[10] = {0};
    for (int i = 0; i < 9; i++) {
        int valor = linea[i];
        if (chequeo[valor] != 0)
            return 1;
        chequeo[valor] = 1;
    }
    return 0;
}

int verificarSubcuadros(int sudoku[9][9]) {
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            int inicioFila = 3 * i;
            int inicioCol = 3 * j;
            int chequeo[10] = {0};
            for (int p = inicioFila; p < inicioFila + 3; p++) {
                for (int q = inicioCol; q < inicioCol + 3; q++) {
                    int valor = sudoku[p][q];
                    if (chequeo[valor] != 0)
                        return 1;
                    chequeo[valor] = 1;
                }
            }
        }
    }
    return 0;
}

int validarSudokuUnHilo(int sudoku[9][9]) {
    for (int i = 0; i < 9; i++) {
        // Verificar cada fila
        if (verificarLinea(sudoku[i]))
            return 1;
        // Verificar cada columna (se arma el vector columna)
        int columna[9];
        for (int j = 0; j < 9; j++) {
            columna[j] = sudoku[j][i];
        }
        if (verificarLinea(columna))
            return 1;
    }
    // Verificar los subcuadros 3x3
    if (verificarSubcuadros(sudoku))
        return 1;
    return 0;
}
