package com.alurachallenge.literalura.principal;

import com.alurachallenge.literalura.model.Autor;
import com.alurachallenge.literalura.model.Libro;
import com.alurachallenge.literalura.model.DatosLibro;
import com.alurachallenge.literalura.model.Resultados;
import com.alurachallenge.literalura.repository.AutorRepository;
import com.alurachallenge.literalura.repository.LibroRepository;
import com.alurachallenge.literalura.service.ConsumoAPI;
import com.alurachallenge.literalura.service.ConvierteDatos;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    List<Libro> libros;
    List<Autor> autores;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    
                    ♥-------------♥ CHALLENGE LITERALURA ♥-------------♥
                    | 1 - Buscar libro por titulo                       |
                    | 2 - Listar libros registrados                     |
                    | 3 - Listar autores registrados                    |
                    | 4 - Listar autores vivos en un determinado año    |
                    | 5 - Listar libros por idioma                      |
                    | 0 - Salir                                         |
                    |---------------------------------------------------|
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosPorDeterminadoAno();
                    break;
                case 5:
                    buscarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private void buscarLibroPorTitulo() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        var datos = conversor.obtenerDatos(json, Resultados.class);
        if (datos.resultados().isEmpty()) {
            System.out.println("El libro no fue encontrado.");
        } else {
            DatosLibro datosLibro = datos.resultados().get(0);
            Libro libro = new Libro(datosLibro);
            Autor autor = new Autor().obtenerPrimerAutor(datosLibro);
            guardarDatos(libro, autor);
        }
    }

    private void guardarDatos(Libro libro, Autor autor) {
        Optional<Libro> libroEncontrado = libroRepository.findByTituloContainsIgnoreCase(libro.getTitulo());
        if (libroEncontrado.isPresent()) {
            System.out.println("El libro ya está registrado");
        } else {
            try {
                libroRepository.save(libro);
                System.out.println("Libro guardo en la base de datos.");
            } catch (Exception e) {
                System.out.println("Error message: " + e.getMessage());
            }
        }

        Optional<Autor> autorEncontrado = autorRepository.findByNombreContains(autor.getNombre());
        if (autorEncontrado.isPresent()) {
            System.out.println("El autor ya está registrado");
        } else {
            try {
                autorRepository.save(autor);
                System.out.println("Autor guardado en la base de datos.");
            } catch (Exception e) {
                System.out.println("Error message: " + e.getMessage());
            }
        }
    }

    private void listarLibrosRegistrados(){
        System.out.println("***-----< Listado de Libros registrados >-----***");
        libros = libroRepository.findAll();
        libros.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(System.out::println);
    }

    private void listarAutoresRegistrados() {
        System.out.println("***-----< Listado de Autores registrados >-----***");
        autores = autorRepository.findAll();
        autores.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void listarAutoresVivosPorDeterminadoAno() {
        System.out.print("Determine el año para listar los autores vivos en ese rango: ");
        Integer edad = Integer.valueOf(teclado.nextLine());
        autores = autorRepository
                .findByAnoDeNacimientoLessThanEqualAndAnoDeFallecimientoGreaterThanEqual(edad, edad);
        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + edad);
        } else {
            autores.stream()
                    .sorted(Comparator.comparing(Autor::getNombre))
                    .forEach(System.out::println);
        }
    }

    private void buscarLibrosPorIdioma() {
        System.out.println("***-----< Listado de Libros por idioma >-----***");
        System.out.println("""
                de - Alemán
                es - Español
                fr - Francés
                it - Italiano
                en - Inglés
                ja - Japonés
                zh - Chino
                pt - Portugués
                """);
        String lengua = teclado.nextLine();
        libros = libroRepository.findByIdiomaContains(lengua);
        if (libros.isEmpty()) {
            System.out.println("No se encontraron Libros en el idioma " + lengua);
        } else {
            libros.stream()
                    .sorted(Comparator.comparing(Libro::getTitulo))
                    .forEach(System.out::println);
        }
    }
}