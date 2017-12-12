Public Type MyType
    MyInt As Integer
    MyString As String
End Type

Public Type AnotherType
    XXX As MyType
    YYY As Integer
End Type

Sub autoopen()
    Dim bla As AnotherType
    With bla
        .YYY = 13
        With .XXX
            .MyInt = 42
            .MyString = "Answer"
        End With
    End With

    Debug.Print bla.XXX.MyInt
    Debug.Print bla.XXX.MyString
    Debug.Print bla.YYY
End Sub
