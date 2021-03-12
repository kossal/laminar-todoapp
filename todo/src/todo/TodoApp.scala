package todo

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom
import org.scalajs.dom.html

object TodoApp {

  sealed trait Action
  case class Add(task: String) extends Action
  case class Update(key: Int, task: String) extends Action
  case class Delete(key: Int) extends Action

  case class Task(id: Int, name: String)

  case class State(
      tasks: Seq[Task]
  )

  val initialState = State(Nil)

  val inputBus = new EventBus[String]
  val actionBus = new EventBus[Action]

  val inputSignal: Signal[String] = inputBus.events.startWith("")
  val stateSignal: Signal[State] = actionBus.events.foldLeft(initialState) {

    case (currentState, Add(task)) =>
      val newTaskList = currentState.tasks :+ Task(
        id = currentState.tasks.length + 1,
        name = task
      )
      currentState.copy(tasks = newTaskList)

    case (currentState, Delete(key)) =>
      currentState.copy(tasks = currentState.tasks.filterNot(_.id == key))

    case (currentState, Update(key, task)) =>
      currentState.copy(
        tasks = currentState.tasks
          .find(_.id == key)
          .get
          .copy(name = task) +: currentState.tasks.filterNot(_.id == key)
      )

    case _ => ???
  }

  def renderTaskCard(
      key: Int,
      initialValue: Task,
      taskSignal: Signal[Task]
  ): ReactiveHtmlElement[html.Div] = {
    val cardInputBus: EventBus[String] = new EventBus[String]
    val cardInputSignal: Signal[String] = cardInputBus.events.startWith("")

    div(
      p(
        styleAttr := "display: inline-block;",
        child.text <-- taskSignal.map(task => s"${task.id}) ${task.name}")
      ),
      button(
        styleAttr := "margin-left: 5px; display: inline-block;",
        "Eliminar",
        onClick.mapTo(Delete(key = key)) --> actionBus.writer
      ),
      input(
        styleAttr := "margin-left: 5px; display: inline-block;",
        placeholder := "Edita el task aca...",
        inContext { thisNode =>
          onInput.mapTo(thisNode.ref.value) --> cardInputBus.writer
        }
      ),
      button(
        styleAttr := "margin-left: 5px; display: inline-block;",
        "Editar",
        inContext(
          _.events(onClick)
            .sample(cardInputSignal)
            .map(input => Update(key = key, task = input)) --> actionBus.writer
        )
      )
    )
  }

  // 1) Poder agregar tarjetas de tasks que quiero hacer
  // 2) Modificar los textos de las tarjetas
  // 3) Eliminar las tarjetas
  val app = div(
    h1("TODO APP"),
    h2("Agrega tus tasks al tablero"),
    input(
      idAttr := "todoText",
      typ := "text",
      placeholder := "Escribe tus tareas aca...",
      inContext { thisNode =>
        onInput.mapTo(thisNode.ref.value) --> inputBus.writer
      }
    ),
    button(
      "Agregar task",
      inContext(
        _.events(onClick).sample(inputSignal).map(Add(_)) --> actionBus.writer
      )
    ),
    div(
      children <-- stateSignal.map(_.tasks).split(_.id)(renderTaskCard)
    )
  )

  def main(args: Array[String]): Unit = {
    render(dom.document.getElementById("root"), app)
  }
}
