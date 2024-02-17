package com.safenet.service.ui.on_boarding.voucher_bottomsheet

sealed class EnterVoucherBottomSheetEvents{
    class NavigateToEnterCode(val code : String) : EnterVoucherBottomSheetEvents()
    object InitViews: EnterVoucherBottomSheetEvents()
    object Success: EnterVoucherBottomSheetEvents()
    object Error: EnterVoucherBottomSheetEvents()
    object
    MaxUserDialog : EnterVoucherBottomSheetEvents()

    class MaxLoginDialog(val message : String) : EnterVoucherBottomSheetEvents()
}
