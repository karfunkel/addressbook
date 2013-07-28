@artifact.package@


import org.aklein.crudlist.*

class @artifact.name@ extends CrudListControllerBase<@artifact.name.plain@Model, @artifact.name.plain@View> {
    @artifact.name@() {
        super('@artifact.name.plain@', 'calledMvcGroup' /* TODO: set called mvc-group */, { /*innerTypeModel*/ model -> /* TODO: access entity */})
    }
}
