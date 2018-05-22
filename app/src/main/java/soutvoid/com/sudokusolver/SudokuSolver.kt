package soutvoid.com.sudokusolver

class SudokuSolver {

    fun solve(i: Int, j: Int, cells: Array<Array<Int>>): Boolean {
        var i = i
        var j = j
        if (i == 9) {
            i = 0
            if (++j == 9)
                return true
        }
        if (cells[i][j] != 0)
        // skip filled cells
            return solve(i + 1, j, cells)

        for (`val` in 1..9) {
            if (legal(i, j, `val`, cells)) {
                cells[i][j] = `val`
                if (solve(i + 1, j, cells))
                    return true
            }
        }
        cells[i][j] = 0 // reset on backtrack
        return false
    }

    fun legal(i: Int, j: Int, `val`: Int, cells: Array<Array<Int>>): Boolean {
        for (k in 0..8)
        // row
            if (`val` == cells[k][j])
                return false

        for (k in 0..8)
        // col
            if (`val` == cells[i][k])
                return false

        val boxRowOffset = i / 3 * 3
        val boxColOffset = j / 3 * 3
        for (k in 0..2)
        // box
            for (m in 0..2)
                if (`val` == cells[boxRowOffset + k][boxColOffset + m])
                    return false

        return true // no violations, so it's legal
    }

    fun parseProblem(args: Array<String>): Array<Array<Int>> {
        val problem = Array(9) { Array<Int>(9) { 0 } } // default 0 vals
        for (n in args.indices) {
            val i = Integer.parseInt(args[n].substring(0, 1))
            val j = Integer.parseInt(args[n].substring(1, 2))
            val `val` = Integer.parseInt(args[n].substring(2, 3))
            problem[i][j] = `val`
        }
        return problem
    }

}