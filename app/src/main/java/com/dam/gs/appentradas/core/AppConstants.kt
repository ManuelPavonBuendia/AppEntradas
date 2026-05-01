package com.dam.gs.appentradas.core

object AppConstants {
    const val URL_ODOO = "https://edu-pruebaeventos.odoo.com"
    const val DB_NAME = "edu-pruebaeventos"

    const val MODEL_EVENTO = "event.event"
    const val MODEL_REGISTRO = "event.registration"

    const val METHOD_SEARCH_READ = "search_read"
    const val METHOD_SET_DONE = "action_set_done"

    const val FIELD_ID = "id"
    const val FIELD_NAME = "name"
    const val FIELD_STATE = "state"
    const val FIELD_BARCODE = "barcode"
    const val FIELD_PARTNER_ID = "partner_id"
    const val FIELD_STAGE_ID_NAME = "stage_id.name"
    const val FIELD_EVENT_ID = "event_id"
    const val FIELD_IMAGE = "image_128"

    const val STAGE_ANUNCIADO = "Anunciado"
    const val OPERATOR_EQUALS = "="
    const val SIN_NOMBRE = "Sin nombre"
}