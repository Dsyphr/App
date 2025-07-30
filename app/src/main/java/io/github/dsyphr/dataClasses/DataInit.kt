package io.github.dsyphr.dataClasses

val joe = User("Joe", 1)
val jake = User("Jake ", id = 2)

val msg1 = MessageItem("Hey, how are you doing", joe, jake)
val msg2 = MessageItem("Hey, how are you doing", jake, joe)
val messageItems = mutableListOf<MessageItem>(msg1, msg2)
