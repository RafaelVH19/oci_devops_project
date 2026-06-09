package com.springboot.MyTodoList.service;

import java.util.ArrayList;
import java.util.List;

/****
 * Clase que representa el plan de acción generado por Lumen para una solicitud de usuario.
 *
 * Contiene la acción identificada por Lumen, así como los datos relevantes extraídos de la solicitud
 * que son necesarios para ejecutar la acción. También incluye información sobre si se necesita
 * aclaración adicional antes de ejecutar la acción, y la pregunta de aclaración si es necesario.
 *
 * Esta clase se utiliza para encapsular toda la información necesaria para que el sistema pueda
 * ejecutar la acción recomendada por Lumen de manera efectiva y manejar cualquier caso en el que
 * se requiera información adicional del usuario.
 */
public class LumiActionPlan {

    /** Enum que define las posibles acciones que Lumen puede identificar a partir de la solicitud del usuario. */
    public enum Action {
        NONE,
        CHAT,
        CREATE_TEAM,
        CREATE_PROJECT,
        CREATE_SPRINT,
        WORKLOAD,
        TASK_QUERY
    }

    private Action action = Action.NONE;
    private String teamName;
    private List<String> memberNames = new ArrayList<>();
    private String managerName;
    private String projectName;
    private String sourceTeamName;
    private String sprintName;
    private String startDate;
    private String endDate;
    private boolean needsClarification;
    private String clarificationQuestion;

    /** Constructor vacío requerido para la deserialización de JSON. */
    public Action getAction() {
        return action;
    }

    /** Establece la acción identificada por Lumen, asegurándose de que si se recibe un valor nulo, se establezca como NONE para evitar problemas en la lógica de ejecución. */
    public void setAction(Action action) {
        this.action = action == null ? Action.NONE : action;
    }

    /** Devuelve el nombre del equipo asociado a la acción. */
    public String getTeamName() {
        return teamName;
    }

    /** Establece el nombre del equipo asociado a la acción. */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /** Devuelve la lista de nombres de los miembros asociados a la acción. */
    public List<String> getMemberNames() {
        return memberNames;
    }

    /** Establece la lista de nombres de los miembros asociados a la acción, asegurándose de que si se recibe una lista nula, se inicialice como una lista vacía para evitar problemas en la lógica de ejecución. */
    public void setMemberNames(List<String> memberNames) {
        this.memberNames = memberNames == null ? new ArrayList<>() : memberNames;
    }

    /** Devuelve el nombre del manager asociado a la acción. */
    public String getManagerName() {
        return managerName;
    }

    /** Establece el nombre del manager asociado a la acción. */
    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    /** Devuelve el nombre del proyecto asociado a la acción. */
    public String getProjectName() {
        return projectName;
    }

    /** Establece el nombre del proyecto asociado a la acción. */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /** Devuelve el nombre del equipo fuente asociado a la acción. */
    public String getSourceTeamName() {
        return sourceTeamName;
    }

    /** Establece el nombre del equipo fuente asociado a la acción. */
    public void setSourceTeamName(String sourceTeamName) {
        this.sourceTeamName = sourceTeamName;
    }

    /** Devuelve el nombre del sprint asociado a la acción. */
    public String getSprintName() {
        return sprintName;
    }

    /** Establece el nombre del sprint asociado a la acción. */
    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    /** Devuelve la fecha de inicio asociada a la acción. */
    public String getStartDate() {
        return startDate;
    }

    /** Establece la fecha de inicio asociada a la acción. */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /** Devuelve la fecha de finalización asociada a la acción. */
    public String getEndDate() {
        return endDate;
    }

    /** Establece la fecha de finalización asociada a la acción. */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /** Devuelve si se necesita aclaración adicional antes de ejecutar la acción. */
    public boolean isNeedsClarification() {
        return needsClarification;
    }

    /** Establece si se necesita aclaración adicional antes de ejecutar la acción. */
    public void setNeedsClarification(boolean needsClarification) {
        this.needsClarification = needsClarification;
    }

    /** Devuelve la pregunta de aclaración si se necesita información adicional antes de ejecutar la acción. */
    public String getClarificationQuestion() {
        return clarificationQuestion;
    }

    /** Establece la pregunta de aclaración si se necesita información adicional antes de ejecutar la acción. */
    public void setClarificationQuestion(String clarificationQuestion) {
        this.clarificationQuestion = clarificationQuestion;
    }

    /** Verifica si la acción identificada por Lumen es ejecutable, lo que significa que no es NONE, CHAT o TASK_QUERY, y que no se necesita aclaración adicional antes de la ejecución. */
    public boolean isExecutable() {
        return action != null
            && action != Action.NONE
            && action != Action.CHAT
            && action != Action.TASK_QUERY
            && !needsClarification;
    }
}
