package views.formFieldTemplates

import views.html.helper.FieldConstructor

object FormFieldTemplate {
  implicit val myFields: FieldConstructor = FieldConstructor(
    views.html.formFieldTemplates.formFieldTemplate(_)
  )
}
