package com.nexo.launcher.ui.subassembly.account

import com.nexo.launcher.value.MinecraftAccount

interface SelectAccountListener {
    fun onSelect(account: MinecraftAccount)
}

