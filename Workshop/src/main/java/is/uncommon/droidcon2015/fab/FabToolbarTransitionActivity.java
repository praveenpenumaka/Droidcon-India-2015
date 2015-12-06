package is.uncommon.droidcon2015.fab;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import is.uncommon.droidcon2015.R;
import is.uncommon.droidcon2015.utils.MathUtils;

/**
 * Created by Madhu on 05/12/15.
 */
public class FabToolbarTransitionActivity extends AppCompatActivity {

    private static final String TAG = FabToolbarTransitionActivity.class.getSimpleName();
    @Bind(R.id.appbar) AppBarLayout mAppBar;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.scroll_view) NestedScrollView mScrollView;
    @Bind(R.id.toolbar_bottom) Toolbar mBottomToolbar;
    @Bind(R.id.demo_button) Button mToggle;

    private boolean mHasFabTransitioned = false;
    private int mActionBarHeight;
    private boolean mIsAnimating = false;
    private float mPrevToggledOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fab_toolbar_transition_demo);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFabAnimation();
            }
        });
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFabAnimation();
            }
        });
        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int scrolledYOffset = v.getScrollY();
            }
        });
        mBottomToolbar.setScaleX(0);
        mBottomToolbar.setScaleY(0);
    }

    private void toggleFabAnimation() {
        mIsAnimating = true;
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        final float maxYTranslation = params.bottomMargin;
        final float maxXTranslation = mScrollView.getWidth() / 2 - mFab.getWidth() / 2 - params.rightMargin;

        final ValueAnimator translation = ValueAnimator.ofFloat(0, 1);
        final float startX = mHasFabTransitioned ? -maxXTranslation : 0;
        final float startY = mHasFabTransitioned ? maxYTranslation : 0;
        final float endX = mHasFabTransitioned ? 0 : -maxXTranslation;
        final float endY = mHasFabTransitioned ? 0 : maxYTranslation;
        final float controlPointX = 0;
        final float controlPointY = maxYTranslation;
        translation.setInterpolator(new AccelerateDecelerateInterpolator());
        translation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                float transX = (float) MathUtils.quadBezierValue(startX, controlPointX, endX, value);
                float transY = (float) MathUtils.quadBezierValue(startY, controlPointY, endY, value);
                mFab.setTranslationX(transX);
                mFab.setTranslationY(transY);
            }
        });
        translation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if (mHasFabTransitioned) {
                    mFab.setScaleY(1);
                    mFab.setScaleX(1);
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!mHasFabTransitioned) {
                    mFab.setTranslationX(endX);
                    mFab.setTranslationY(endY);
                    mFab.setScaleY(0);
                    mFab.setScaleX(0);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        translation.setDuration(150);
        //reveal animation
        Animator revealAnimator = null;
        AnimatorSet animatorSet = new AnimatorSet();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            revealAnimator = ViewAnimationUtils.createCircularReveal(mBottomToolbar, mBottomToolbar.getWidth() / 2, mBottomToolbar.getHeight() / 2, mHasFabTransitioned ? mBottomToolbar.getWidth() : 0, mHasFabTransitioned ? 0 : mBottomToolbar.getWidth());
            revealAnimator.setDuration(150);
            revealAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mBottomToolbar.setScaleX(1);
                    mBottomToolbar.setScaleY(1);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (mHasFabTransitioned) {
                        mBottomToolbar.setScaleX(0);
                        mBottomToolbar.setScaleY(0);
                    }

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        } else {
            ValueAnimator animtr = ValueAnimator.ofFloat(0, 1);
            animtr.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float scale = (float) valueAnimator.getAnimatedValue();
                    mBottomToolbar.setScaleX(scale);
                    mBottomToolbar.setScaleY(scale);
                }
            });
            animtr.setDuration(300);
            revealAnimator = animtr;
        }
        if (!mHasFabTransitioned) {
            animatorSet.play(translation).before(revealAnimator);
        } else {
            animatorSet.play(revealAnimator).before(translation);
        }

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mHasFabTransitioned = !mHasFabTransitioned;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}