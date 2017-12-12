Sub autoopen()
    Open "/tmp/thisisasecretfile" For Output as haha
    Write #haha, "File write ok"
    Print #haha, Spc(5); "Zone 1"; Tab; "Zone 2" Tab(4) "Zone 3" " "; "Zone 4"
    Close #haha
    Open "/tmp/thisisasecretfile" For Input as 1
    Get #1,, A
    Line Input #1, B
    CLOSE #1
End Sub
