package vn.techres.line.data.model.utils

class Base {
    interface Factory<T> {
        fun create(): T
    }

    companion object : Factory<Base> {
        override fun create(): Base = Base()
    }
}