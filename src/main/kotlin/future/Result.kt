package future

public trait Result
public class Success<T>(val r: T) : Result
public class Failure() : Result