package com.rahulpandey.memory

import android.animation.ArgbEvaluator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rahulpandey.memory.databinding.ActivityMainBinding
import com.rahulpandey.memory.databinding.DialogBoardSizeBinding

private const val TAG = "MainActivity-Truong"
private const val CREATE_REQUEST_CODE = 1967

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MemoryAdapter
    private lateinit var game: Game
    private var boardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // hack to short-cut straight to CreateActivity
        val intent = Intent(this, CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.MEDIUM)
        startActivity(intent)

        setupGame()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_refresh -> {
                if (game.moves() > 0 && !game.won()) {
                    showAlertDialog("Quit your current game?", null, View.OnClickListener {
                        setupGame()
                    })
                } else {
                    setupGame()
                }
                return true
            }
            R.id.menu_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.menu_custom -> {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        val bind = DialogBoardSizeBinding.inflate(LayoutInflater.from(this))
        showAlertDialog("Create your own game", bind.root, View.OnClickListener {
            // set a new value for the board size
            val customSize = when(bind.radioGroup.checkedRadioButtonId) {
                R.id.radio_easy -> BoardSize.EASY
                R.id.radio_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            // navigate to a new Activity
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, customSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        // val view = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val bind = DialogBoardSizeBinding.inflate(LayoutInflater.from(this))
        when (boardSize) {
            BoardSize.EASY -> bind.radioGroup.check(R.id.radio_easy)
            BoardSize.MEDIUM -> bind.radioGroup.check(R.id.radio_medium)
            BoardSize.HARD -> bind.radioGroup.check(R.id.radio_hard)
        }

        showAlertDialog("Choose new size", bind.root, View.OnClickListener {
            // set a new value for the board size
            boardSize = when(bind.radioGroup.checkedRadioButtonId) {
                R.id.radio_easy -> BoardSize.EASY
                R.id.radio_medium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupGame()
        })
    }

    private fun showAlertDialog(title: String, view: View?, clickListener: View.OnClickListener) {
        AlertDialog.Builder(this).setTitle(title).setView(view).setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                clickListener.onClick(null)
            }.show()
    }

    private fun setupGame() {
        when (boardSize) {
            BoardSize.EASY -> {
                binding.moves.text = "Easy: 4 x 2"
                binding.pairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                binding.moves.text = "Easy: 6 x 3"
                binding.pairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                binding.moves.text = "Easy: 6 x 4"
                binding.pairs.text = "Pairs: 0 / 12"
            }
        }

        binding.pairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        game = Game(boardSize)

        adapter = MemoryAdapter(boardSize, game.cards, object : MemoryAdapter.ClickListener {
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }
        })
        binding.board.adapter = adapter
        binding.board.setHasFixedSize(true)
        binding.board.layoutManager = GridLayoutManager(this, boardSize.width())
    }

    private fun updateGameWithFlip(position: Int) {
        // error handling
        if (game.won()) {
            Snackbar.make(binding.root, "You already won", Snackbar.LENGTH_LONG).show()
            return
        }
        if (game.isFaceUp(position)) {
            Snackbar.make(binding.root, "Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }

        // flip over the card
        if (game.flipCard(position)) {
            Log.d(TAG, "Found a match. number of pairs: ${game.pairsFound}")
            val color = ArgbEvaluator().evaluate(
                game.pairsFound / boardSize.pairs().toFloat(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            binding.pairs.setTextColor(color)
            binding.pairs.text = "Pairs: ${game.pairsFound} / ${boardSize.pairs()}"
            if (game.won()) {
                Snackbar.make(binding.root, "You won! Congratulations!", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        binding.moves.text = "Moves: ${game.moves()}"
        adapter.notifyDataSetChanged()
    }
}