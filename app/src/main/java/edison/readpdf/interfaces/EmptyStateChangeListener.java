package edison.readpdf.interfaces;

public interface EmptyStateChangeListener {
    void setEmptyStateVisible();
    void setEmptyStateInvisible();
    void showNoPermissionsView();
    void hideNoPermissionsView();
    void filesPopulated();
}
