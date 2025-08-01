package io.github.dsyphr.dataClasses

val joe = Contact("Joe", 0)
val jake = Contact("Jake ",   1)
val users = mutableListOf<Contact>(joe,jake)
val msg1 = MessageItem("Hey, how are you doing", joe)
val msg2 = MessageItem("Im good wbu?", jake)
val messageItems = mutableListOf<MessageItem>(msg1, msg2)


val messages = mutableListOf<MessageItem>(msg1,msg2)
