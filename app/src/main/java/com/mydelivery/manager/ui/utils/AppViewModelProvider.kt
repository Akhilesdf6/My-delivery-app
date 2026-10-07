package com.mydelivery.manager.ui.utils

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mydelivery.manager.MyDeliveryManagerApplication
import com.mydelivery.manager.ui.viewmodels.CodViewModel
import com.mydelivery.manager.ui.viewmodels.CustomerViewModel
import com.mydelivery.manager.ui.viewmodels.DeliveryViewModel
import com.mydelivery.manager.ui.viewmodels.HomeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val app = myDeliveryManagerApplication()
            HomeViewModel(app.container.shipmentRepository)
        }
        initializer {
            val app = myDeliveryManagerApplication()
            DeliveryViewModel(app.container.shipmentRepository)
        }
        initializer {
            val app = myDeliveryManagerApplication()
            CustomerViewModel(app.container.customerRepository)
        }
        initializer {
            val app = myDeliveryManagerApplication()
            CodViewModel(app.container.codRepository)
        }
    }
}

fun CreationExtras.myDeliveryManagerApplication(): MyDeliveryManagerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyDeliveryManagerApplication)
