package com.springboot.MyTodoList.model;


import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Arrays;

/*
    representation of the TODOITEM table that exists already
    in the autonomous database
 */
@Entity
@Table(name = "TODOITEM")
public class ToDoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int ID;
    @Column(name = "DESCRIPTION")
    String description;
    @Column(name = "CREATION_TS")
    OffsetDateTime creation_ts;
    @Column(name = "done")
    boolean done;

    //ALTER TABLE TODOITEM ADD (INSIGHT VECTOR);
    @Transient
    float[] insight;

    /** Constructor por defecto. */
    public ToDoItem(){

    }

    /** Constructor que inicializa todos los campos de la entidad. */
    public ToDoItem(int ID, String description, OffsetDateTime creation_ts, boolean done) {
        this.ID = ID;
        this.description = description;
        this.creation_ts = creation_ts;
        this.done = done;
    }

    /** Obtiene el ID del ítem de la lista de tareas. */
    public int getID() {
        return ID;
    }

    /** Establece el ID del ítem de la lista de tareas. */
    public void setID(int ID) {
        this.ID = ID;
    }

    /** Obtiene la descripción del ítem de la lista de tareas. */
    public String getDescription() {
        return description;
    }

    /** Obtiene los insights del ítem de la lista de tareas. */
    public float[] getInsight() {
        return insight;
    }

    /** Establece la descripción del ítem de la lista de tareas. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Obtiene la fecha de creación del ítem de la lista de tareas. */
    public OffsetDateTime getCreation_ts() {
        return creation_ts;
    }

    /** Establece la fecha de creación del ítem de la lista de tareas. */
    public void setCreation_ts(OffsetDateTime creation_ts) {
        this.creation_ts = creation_ts;
    }

    /** Obtiene el estado de finalización del ítem de la lista de tareas. */
    public boolean isDone() {
        return done;
    }

    /** Establece el estado de finalización del ítem de la lista de tareas. */
    public void setDone(boolean done) {
        this.done = done;
    }

    /** Establece los insights del ítem de la lista de tareas. */
    public void setInsight(float[] insight) {
        this.insight = insight;
    }

    /** Devuelve una representación en forma de cadena del ítem de la lista de tareas. */
    @Override
    public String toString() {
        return "ToDoItem{" +
                "ID=" + ID +
                ", description='" + description + '\'' +
                ", creation_ts=" + creation_ts +
                ", done=" + done +
                ", insight=" + Arrays.toString(insight) +
                '}';
    }
}
