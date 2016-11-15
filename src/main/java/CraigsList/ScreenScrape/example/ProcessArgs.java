package CraigsList.ScreenScrape.example;

/**
 * Created by tim on 11/14/2016.
 */
public class ProcessArgs {

    private String mCityName = null;
    private int mMaxPrice = 0;
    private int mMinPrice = 0;

    public ProcessArgs(String[] args){
        for (String arg : args){
            if (arg.matches("[a-zA-Z]+")){
                mCityName = arg;
            }
            if (arg.matches("[+-]?\\d*(\\.\\d+)?")&& mMaxPrice == 0){
                mMaxPrice = Integer.parseInt(arg);
            }
            else if (arg.matches("[+-]?\\d*(\\.\\d+)?")){
                mMinPrice = Integer.parseInt(arg);
            }
        }
        if (mMinPrice > mMaxPrice){
            int temp = mMaxPrice;
            mMaxPrice = mMinPrice;
            mMinPrice = temp;
        }
    }

    public String getCityName(){
        return mCityName;
    }
    public int getMaxPrice(){
        return mMaxPrice;
    }
    public int getMinPrice(){
        return mMinPrice;
    }
}
