package com.rahulpandey.memory

class Game(val boardSize: BoardSize) {
    val cards: List<Card>
    var pairsFound = 0
    private var selectedIndex: Int? = null
    private var cardFlips = 0

    init {
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.pairs())
        val shuffledImages = (chosenImages + chosenImages).shuffled()
        cards = shuffledImages.map { Card(it) }
    }

    fun flipCard(position: Int): Boolean {
        cardFlips++

        // Three scenarios:
        // 0 cards previously flipped over: flip over the selected card
        // 1 card previously flipped over: flip over the selected card, plus check if the images match
        // 2 cards previously flipped over: restore cards, then flip over the selected card
        var foundMatch = false
        if (selectedIndex == null) {
            // 0 or 2 cards previously flipped over
            restoreCards()
            selectedIndex = position
        } else {
            // exactly 1 card previously flipped over
            foundMatch = checkForMatch(selectedIndex!!, position)
            selectedIndex = null
        }
        cards[position].isFaceUp = !cards[position].isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].id != cards[position2].id) {
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        pairsFound++
        return true
    }

    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun won() = pairsFound == boardSize.pairs()

    fun isFaceUp(position: Int) = cards[position].isFaceUp

    fun moves() = cardFlips / 2
}