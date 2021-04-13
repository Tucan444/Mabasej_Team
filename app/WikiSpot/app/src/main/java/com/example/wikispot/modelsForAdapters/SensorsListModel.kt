package com.example.wikispot.modelsForAdapters

data class LabeledValue(val label: String, val value: String)


object LabeledValuesSupplier {

    var labeledValues = arrayOf<LabeledValue?>()

    fun appendLabeledValue(labeledValue: LabeledValue) {
        val array = labeledValues.copyOf(labeledValues.size + 1)
        array[labeledValues.size] = labeledValue
        labeledValues = array
    }

    fun checkIfContains(labeledValue: LabeledValue): Boolean{
        for (n in labeledValues.indices) {
            if (labeledValues[n]!!.label == labeledValue.label) {
                return true
            }
        }
        return false
    }

    fun wipeData() {
        labeledValues = arrayOf<LabeledValue?>()
    }

}

