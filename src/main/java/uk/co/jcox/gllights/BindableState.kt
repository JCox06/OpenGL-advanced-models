package uk.co.jcox.gllights

interface BindableState {
    fun bind()

    fun unbind()

    fun destroy()
}