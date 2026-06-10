package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.repository.ToDoItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate; // Vector
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Vector
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.time.OffsetDateTime;

import java.sql.Types;
import java.io.StringReader;

/** Servicio encargado de gestionar la lógica de negocio relacionada con los elementos de la lista de tareas, incluyendo operaciones CRUD para los elementos almacenados en la base de datos a través del ToDoItemRepository. */
@Service
public class ToDoItemService {

    private final ToDoItemRepository toDoItemRepository;
    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    /** Constructor para inyección de dependencias, incluyendo el repositorio de ToDoItem, el modelo de embedding y JdbcTemplate para operaciones de base de datos personalizadas. */
    @Autowired
    public ToDoItemService(ToDoItemRepository repository, EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        this.toDoItemRepository = repository;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Devuelve una lista de todos los elementos de la lista de tareas almacenados en la base de datos. */
    public List<ToDoItem> findAll(){
        return toDoItemRepository.findAll();
    }

    /** Devuelve un ResponseEntity que contiene el elemento de la lista de tareas con el ID especificado si existe, o un estado NOT_FOUND si no se encuentra. */
    public ResponseEntity<ToDoItem> getItemById(int id){
        Optional<ToDoItem> todoData = toDoItemRepository.findById(id);
        if (todoData.isPresent()){
            return new ResponseEntity<>(todoData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /** Devuelve el elemento de la lista de tareas con el ID especificado si existe, o null si no se encuentra. */
    public ToDoItem getToDoItemById(int id){
        return toDoItemRepository.findById(id).orElse(null);
    }
    
    /** Agrega un nuevo elemento de la lista de tareas a la base de datos, generando un vector de embedding para la descripción y almacenándolo en la columna INSIGHT, y devuelve el elemento de la lista de tareas guardado. */
    @Transactional // Importante para operaciones de base de datos personalizadas
    public ToDoItem addToDoItem(ToDoItem toDoItem){
        // 1. Generar el vector
        float[] vector = embeddingModel.embed(toDoItem.getDescription());
        String vectorStr = convertVectorToString(vector);
        
        // 2. Usar JdbcTemplate en lugar del repositorio para evitar el error ORA-01461
        this.saveToDoWithVector(
            toDoItem.getDescription(), 
            OffsetDateTime.now(), 
            toDoItem.isDone(), // Usamos el valor real del objeto en vez de 'false' hardcodeado
            vectorStr
        );
        
        return toDoItem;
    }

    /** Elimina el elemento de la lista de tareas con el ID especificado de la base de datos, devolviendo true si la eliminación fue exitosa o false si ocurrió un error. */
    public boolean deleteToDoItem(int id){
        try {    
            toDoItemRepository.deleteById(id);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /** Actualiza un elemento de la lista de tareas existente en la base de datos, generando un nuevo vector de embedding para la descripción actualizada y almacenándolo en la columna INSIGHT, y devuelve el elemento de la lista de tareas actualizado. */
    public ToDoItem updateToDoItem(int id, ToDoItem td){
        Optional<ToDoItem> toDoItemData = toDoItemRepository.findById(id);
        if(toDoItemData.isPresent()){
            ToDoItem toDoItem = toDoItemData.get();
            toDoItem.setID(id);
            toDoItem.setCreation_ts(td.getCreation_ts());
            toDoItem.setDescription(td.getDescription());
            toDoItem.setDone(td.isDone());
            return toDoItemRepository.save(toDoItem);
        } else {
            return null;
        }
    }

    /** Método privado para convertir un vector de float a su representación String en formato de array, que es compatible con la función TO_VECTOR de Oracle 23ai. */
    private String convertVectorToString(float[] vector) {
    // Arrays.toString() devuelve por defecto el formato "[1.0, 2.0, 3.0]" 
    // que es exactamente la sintaxis que Oracle 23ai exige.
        return Arrays.toString(vector);
    }

    /** Método privado que utiliza JdbcTemplate para insertar un nuevo elemento de la lista de tareas con su vector de embedding, evitando el error ORA-01461 al forzar el uso de CLOB para el campo INSIGHT. */
    public void saveToDoWithVector(String desc, OffsetDateTime ts, boolean done, String vectorStr) {
        String sql = "INSERT INTO TODOITEM (DESCRIPTION, CREATION_TS, DONE, INSIGHT) " +
                    "VALUES (?, ?, ?, TO_VECTOR(?))"; // Ya no necesitamos TO_CLOB aquí, Java lo enviará como CLOB nativo.
        
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, desc);
            ps.setObject(2, ts);
            ps.setBoolean(3, done);
            
            // ¡LA MAGIA OCURRE AQUÍ!
            // Al usar setClob con un StringReader, el driver de Oracle se ve obligado 
            // a transmitir el texto pesado como un CLOB sin importar su longitud.
            ps.setClob(4, new StringReader(vectorStr)); 
        });
    }
}   