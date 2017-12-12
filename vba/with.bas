Public Type MyType
    MyInt As Integer
    MyString As String
End Type

Sub autoopen()
    Dim bla As MyType
'    bla.MyInt = 42
'    bla.MyString = "Answer"
    With bla
        .MyInt = 42
        .MyString = "Answer"
    End With

    Debug.Print bla.MyInt
    Debug.Print bla.MyString
End Sub
